package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.config.BookingProperties;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.dto.DeskColorDTO;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskColor;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DeskRepository deskRepository;
    private final BookingMapper bookingMapper;
    private final BookingTimeLimitsService bookingTimeLimitsService;
    private final BookingProperties bookingProperties;

    @Transactional
    public void createBooking(String email, BookingCreateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NO_FOUND", "user not found with that email"));
        Desk desk = deskRepository.findById(request.getDeskId()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk not found with thah id"));
        validateDeskType(desk);
        validateBookingLogic(user, request);
        bookingRepository.save(
                Booking.builder()
                        .user(user)
                        .desk(desk)
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .status(resolveStatus(request.getStartTime()))
                        .build());
    }

    private void validateBookingLogic(User user, BookingCreateRequest request) {
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();
        validateBookingTimes(start, end);
        validateOfficeHours(start, end);
        validateMaxDaysInAdvance(start);
        checkDeskAvailability(request.getDeskId(), start, end);
        checkUserAvailability(user.getId(), start, end);
        validateWeeklyHoursLimit(user.getId(), start, end);

        log.info("Validation passed for user {} desk {}", user.getEmail(), request.getDeskId());
    }

    private void validateDeskType(Desk desk) {
        if (desk.getType() == DeskType.ASSIGNED || desk.getType() == DeskType.UNAVAILABLE) {
            log.error("Desk {} is not available for booking (type: {})", desk.getId(), desk.getType());
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "DESK_NOT_BOOKABLE",
                    String.format("Desk %d cannot be booked because it is %s", desk.getId(), desk.getType())
            );
        }
        log.debug("Desk {} passed type validation ({})", desk.getId(), desk.getType());
    }

    private void validateOfficeHours(LocalDateTime start, LocalDateTime end) {
        int officeStart = bookingProperties.getOfficeStartHour();
        int officeEnd = bookingProperties.getOfficeEndHour();

        if (start.getHour() < bookingProperties.getOfficeStartHour() || (end.getHour() > officeEnd ||
                (end.getHour() == officeEnd && end.getMinute() > 0))) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "OUTSIDE_OFFICE_HOURS",
                    String.format("Booking must be within office hours (%02d:00–%02d:00)", officeStart, officeEnd)
            );
        }
        log.debug("Booking is within office hours ({}–{})", officeStart, officeEnd);
    }

    public void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime.isBefore(now)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before current time");
        }

        if (endTime.isBefore(startTime)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "End time must be after start time");
        }

        long effectiveHours = effectiveHoursExcludingLunch(startTime, endTime);

        if (effectiveHours < bookingProperties.getMinHours()) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "TOO_SHORT",
                    "Booking must be at least " + bookingProperties.getMinHours() + " hour (excluding lunch)");
        }
        if (effectiveHours > bookingProperties.getMaxHours()) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "TOO_LONG",
                    "Cannot book more than " + bookingProperties.getMaxHours() + " hours (excluding lunch)");
        }
        log.debug("validateBookingTimes OK — effective hours: {}", effectiveHours);
    }

    private long effectiveHoursExcludingLunch(LocalDateTime start, LocalDateTime end) {
        long hours = Duration.between(start, end).toHours();
        LocalDateTime lunchStart = start.withHour(bookingProperties.getLunchStartHour());
        LocalDateTime lunchEnd = start.withHour(bookingProperties.getLunchEndHour());

        if (start.isBefore(lunchEnd) && end.isAfter(lunchStart)) {
            hours -= 1;
            log.debug("Lunch overlap detected → -1 hour applied");
        }
        return Math.max(hours, 0);
    }

    private void checkDeskAvailability(Long deskId, LocalDateTime start, LocalDateTime end) {
        if (!bookingRepository.findOverlappingBookings(deskId, start, end).isEmpty()) {
            throw new ExceptionResponse(
                    HttpStatus.CONFLICT,
                    "DESK_NOT_AVAILABLE",
                    "Desk already booked in this period"
            );
        }
    }


    private void validateMaxDaysInAdvance(LocalDateTime startTime) {
        BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();
        LocalDateTime now = LocalDateTime.now();

        long daysInAdvance = ChronoUnit.DAYS.between(now.toLocalDate(), startTime.toLocalDate());

        if (daysInAdvance > policy.getMaxDaysInAdvance()) {
            log.error("Booking {} days in advance exceeds maximum of {} days",
                    daysInAdvance, policy.getMaxDaysInAdvance());
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "BOOKING_TOO_FAR_AHEAD",
                    String.format("Cannot book more than %d days in advance. You are trying to book %d days ahead.",
                            policy.getMaxDaysInAdvance(), daysInAdvance)
            );
        }

        log.debug("Days in advance validated: {} days (max: {})", daysInAdvance, policy.getMaxDaysInAdvance());
    }

    private void validateWeeklyHoursLimit(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();

        LocalDateTime weekStart = startTime.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        log.debug("Checking weekly hours limit for user {} in week {} to {}",
                userId, weekStart, weekEnd);

        List<Booking> weeklyBookings = bookingRepository.findUserBookings(
                userId, weekStart, weekEnd
        );

        long bookedHours = weeklyBookings.stream()
                .mapToLong(b -> Duration.between(b.getStartTime(), b.getEndTime()).toHours())
                .sum();

        long newBookingHours = Duration.between(startTime, endTime).toHours();
        long totalHours = bookedHours + newBookingHours;

        log.debug("User {} has {} hours booked this week, requesting {} more hours (total: {})",
                userId, bookedHours, newBookingHours, totalHours);

        if (totalHours > policy.getMaxHoursPerWeek()) {
            log.error("User {} would exceed weekly limit: {} hours (max: {})",
                    userId, totalHours, policy.getMaxHoursPerWeek());
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "WEEKLY_HOURS_EXCEEDED",
                    String.format("Cannot exceed %d hours per week. You have %d hours booked and are requesting %d more hours.",
                            policy.getMaxHoursPerWeek(), bookedHours, newBookingHours)
            );
        }

        log.debug("Weekly hours limit validated successfully for user {}", userId);
    }

    public void checkUserAvailability(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!bookingRepository.findAnyUserConflict(userId, startTime, endTime).isEmpty())
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_CONFLICT", "You already have a booking");
    }

    private BookingStatus resolveStatus(LocalDateTime start) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(start)) {
            return BookingStatus.SCHEDULED;
        }
        return BookingStatus.ACTIVE;
    }


    @Transactional
    public void cancelBooking(String email, Long bookingId) {
        Booking bookingToCancel = bookingRepository.findByUserEmailAndId(email, bookingId)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found"));
        if (bookingToCancel.getStatus() != BookingStatus.ACTIVE) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_BOOKING_STATUS",
                    "Only active bookings can be cancelled"
            );
        }
        bookingToCancel.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(bookingToCancel);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(String email) {

        List<Booking> active = bookingRepository
                .findByUserEmailAndStatusOrderByStartTimeAsc(email, BookingStatus.ACTIVE);

        List<Booking> scheduled = bookingRepository
                .findByUserEmailAndStatusOrderByStartTimeAsc(email, BookingStatus.SCHEDULED);

        return Stream.concat(active.stream(), scheduled.stream())
                .map(bookingMapper::toResponse)
                .toList();
    }


    private void updateDeskStatus(Desk desk, DeskStatus status) {
        log.debug("Updating desk id: {} status to {}", desk.getId(), status);
        desk.setStatus(status);
        deskRepository.save(desk);
        log.info("Desk id: {} status updated to {}", desk.getId(), status);
    }


    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(String email) {

        log.info("Fetching bookings for user: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            log.warn("User not found for email: {}", email);
            return new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User does not exist");
        });

        List<Booking> bookings = bookingRepository.findBookingsByUserOrderByStartTimeDesc(user);

        log.info("Found {} bookings for user: {}", bookings.size(), email);

        List<BookingResponse> responses = bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Mapped {} bookings to response DTOs for user: {}", responses.size(), email);

        return responses;
    }


    public BookingResponseDto getBookingById(String email, Long booking_id) {
        log.info("Fetching booking id: {} for user: {}", booking_id, email);

        Booking booking = bookingRepository.findById(booking_id).orElseThrow(() -> {
            log.error("Booking not found with id: {}", booking_id);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_BOOKING_FOUND", "Cannot find booking");
        });

        if (!booking.getUser().getEmail().equals(email)) {
            log.error("User {} attempted to access booking {} belonging to another user", email, booking_id);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_NOT_AVAILABLE", "Booking is not available for user with email " + email);
        }

        log.info("Booking id: {} retrieved successfully for user: {}", booking_id, email);
        return bookingMapper.maptoDto(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    public List<BookingDTO> getBookingsByDate(
            LocalDate localDate
    ) {

        log.info(
                "looking for bookings at time {}",
                localDate
        );
        List<Booking> bookings = bookingRepository
                .findBookingsByDate(localDate);

        if (bookings == null || bookings.isEmpty()) {
            log.warn(
                    "Bookings at time {} was not found ",
                    localDate
            );
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "BOOKINGS_NOT_FOUND",
                    String.format(
                            "Bookings for %s date not found",
                            localDate.toString()
                    )
            );
        }
        Map<Long, List<Booking>> bookingsByDesk = bookings
                .stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getDesk().getId())
                );
        List<BookingDTO> resultList = new ArrayList<>();

        for (Map.Entry<Long, List<Booking>> entry : bookingsByDesk.entrySet()) {
            Long deskId = entry.getKey();
            List<Booking> deskBookings = entry.getValue();

            long totalDuration = 0;

            for (Booking booking : deskBookings) {
                BookingDTO bookingDTO = new BookingDTO();
                DeskColorDTO deskColorDTO = new DeskColorDTO();
                deskColorDTO.setDeskId(deskId);
                bookingDTO.setStartDate(booking.getStartTime());
                bookingDTO.setEndDate(booking.getEndTime());

                if(booking.getDesk().getStatus() == DeskStatus.DEACTIVATED){
                    deskColorDTO.setDeskColor(DeskColor.GRAY);
                    continue;
                }

                totalDuration += Duration
                        .between(
                                booking.getStartTime(),
                                booking.getEndTime()
                        ).toHours();

                if (totalDuration > 0 && totalDuration < 9) {
                    deskColorDTO.setDeskColor(DeskColor.AMBER);
                } else if (totalDuration >= 9) {
                    deskColorDTO.setDeskColor(DeskColor.RED);
                }
                log.info(
                        "total duration is {} for desk with id {} and color {}",
                        totalDuration,
                        deskId,
                        deskColorDTO.getDeskColor()
                );
                bookingDTO.setDeskColorDTO(deskColorDTO);
                resultList.add(bookingDTO);
            }
        }
        return resultList;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllUserBookingsByDate(
            String email,
            LocalDate localDate
    ){
        log.info(
                "Looking for user with email {}",
                email
        );
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(()-> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        String.format("User with email: %s is not found", email)
                ));

        log.info(
                "Looking for bookigs with user_id {} and start date {}",
                user.getId(),
                localDate
        );

        List<Booking> bookings = bookingRepository
                .findUserBookingsByDate(
                        user.getId(),
                        localDate
                );

        if(bookings == null || bookings.isEmpty()){
            log.warn(
                    "Bookings with user_id {} and start date {} was not found",
                    user.getId(),
                    localDate
            );

            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "BOOKINGS_BY_DATE_NOT_FOUND",
                    String.format(
                            "Bookings by date %s for user with id %d not found",
                            localDate.toString(),
                            user.getId()
                    )
            );
        }
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }
}