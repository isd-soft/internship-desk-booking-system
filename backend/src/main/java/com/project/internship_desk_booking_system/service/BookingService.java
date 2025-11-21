package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.dto.DeskColorDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
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

import java.time.*;
import java.util.*;
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
    private final BookingServiceValidation bookingValidation;
    private final EmailService emailService;

    @Transactional
    public void createBooking(String email, BookingCreateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NO_FOUND", "user not found with that email"));

        Desk desk = deskRepository.findById(request.getDeskId())
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk not found with that id"));
        bookingValidation.validateBookingLogic(user, request);

        bookingValidation.validateDeskType(desk, request.getStartTime(), request.getEndTime());

        Booking newBooking = Booking.builder()
                .user(user)
                .desk(desk)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(bookingValidation.resolveStatus(request.getStartTime()))
                .build();

        bookingRepository.save(newBooking);

        emailService.sendBookingConfirmationEmail(
                email,
                newBooking.getId(),
                newBooking.getDesk().getDeskName(),
                newBooking.getDesk().getZone().getZoneAbv(),
                OffsetDateTime.now()
        );
    }


    @Transactional
    public void cancelBooking(String email, Long bookingId) {
        Booking bookingToCancel = bookingRepository.findByUserEmailAndId(email, bookingId)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found"));
        if (bookingToCancel.getStatus() != BookingStatus.ACTIVE
                && bookingToCancel.getStatus() != BookingStatus.SCHEDULED) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_BOOKING_STATUS",
                    "Only active bookings can be cancelled"
            );
        }
        bookingToCancel.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(bookingToCancel);
        emailService.sendCancelledBookingEmail(email, bookingToCancel.getId(), bookingToCancel.getDesk().getDeskName(), bookingToCancel.getDesk().getZone().getZoneAbv(), OffsetDateTime.now());
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

    public List<BookingDTO> getBookingsByDate(LocalDate localDate) {
        log.info("looking for bookings at time {}", localDate);

        List<Booking> bookings = bookingRepository.findBookingsByDate(localDate);

        if (bookings == null || bookings.isEmpty()) {
            log.warn("Bookings at time {} was not found ", localDate);
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "BOOKINGS_NOT_FOUND",
                    String.format("Bookings for %s date not found", localDate.toString())
            );
        }

        Map<Long, List<Booking>> bookingsByDesk = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getDesk().getId()));

        List<BookingDTO> resultList = new ArrayList<>();

        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        LocalTime lunchStart = LocalTime.of(13, 0);
        LocalTime lunchEnd = LocalTime.of(14, 0);
        LocalTime now = LocalTime.now();
        boolean isToday = localDate.equals(LocalDate.now());

        LocalTime interestStart = workStart;
        if (isToday) {
            if (now.isAfter(interestStart)) interestStart = now;
            if (!interestStart.isBefore(lunchStart) && interestStart.isBefore(lunchEnd)) {
                interestStart = lunchEnd;
            }
        }

        long secondsUntilEnd = Duration.between(interestStart, workEnd).getSeconds();

        if (interestStart.isBefore(lunchStart)) {
            secondsUntilEnd -= 3600;
        }
        double hoursRequiredToBlock = Math.max(0, secondsUntilEnd / 3600.0);

        for (Map.Entry<Long, List<Booking>> entry : bookingsByDesk.entrySet()) {
            Long deskId = entry.getKey();
            List<Booking> deskBookings = entry.getValue();

            double totalHours = 0;
            double blockedHours = 0;

            boolean isDeactivated = false;
            LocalDateTime minStart = null;
            LocalDateTime maxEnd = null;

            for (Booking booking : deskBookings) {
                if (booking.getDesk().getStatus() == DeskStatus.DEACTIVATED) {
                    isDeactivated = true; break;
                }
                if (booking.getStatus() == BookingStatus.CANCELLED) continue;


                if (minStart == null || booking.getStartTime().isBefore(minStart)) minStart = booking.getStartTime();
                if (maxEnd == null || booking.getEndTime().isAfter(maxEnd)) maxEnd = booking.getEndTime();

                LocalTime bStart = booking.getStartTime().toLocalTime();
                LocalTime bEnd = booking.getEndTime().toLocalTime();

                totalHours += Duration.between(bStart, bEnd).toHours();

                LocalTime effectiveStart = bStart.isAfter(interestStart) ? bStart : interestStart;
                LocalTime effectiveEnd = bEnd.isBefore(workEnd) ? bEnd : workEnd;

                if (effectiveStart.isBefore(effectiveEnd)) {
                    blockedHours += Duration
                            .between(
                                    effectiveStart,
                                    effectiveEnd
                            ).getSeconds() / 3600.0;
                }
            }

            DeskColorDTO deskColorDTO = new DeskColorDTO();
            deskColorDTO.setDeskId(deskId);

            boolean isFullyBlocked = Math.abs(blockedHours - hoursRequiredToBlock) < 0.01;

            if (isDeactivated) {
                deskColorDTO.setDeskColor(DeskColor.GRAY);
            }
            else if (totalHours >= 9 || (hoursRequiredToBlock > 0 && isFullyBlocked)) {
                deskColorDTO.setDeskColor(DeskColor.RED);
            }
            else if (totalHours > 0) {
                deskColorDTO.setDeskColor(DeskColor.AMBER);
            }

            if (minStart != null || isDeactivated) {
                BookingDTO bookingDTO = new BookingDTO();
                bookingDTO.setStartDate(minStart);
                bookingDTO.setEndDate(maxEnd);
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
    ) {
        log.info(
                "Looking for user with email {}",
                email
        );
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(
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
                .findUserBookingsByDateNotCancelled(
                        user.getId(),
                        localDate
                );

        if (bookings == null || bookings.isEmpty()) {
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

    public List<String> getBookingStatusEnum() {
        return Arrays.stream(BookingStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}