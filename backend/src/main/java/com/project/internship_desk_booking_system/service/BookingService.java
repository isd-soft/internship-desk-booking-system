package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DeskRepository deskRepository;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;

    private static final int MIN_BOOKING_HOURS = 1;
    private static final int MAX_BOOKING_HOURS = 8;

    public BookingResponseDto createBooking(String email, BookingCreateRequest request) {
        log.info("Creating booking for user: {} for desk: {}", email, request.getDeskId());

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user with email " + email);
        });

        Desk desk = deskRepository.findById(request.getDeskId()).orElseThrow(() -> {
            log.error("Desk not found with id: {}", request.getDeskId());
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_DESKID_FOUND", "Cannot find desk id");
        });

        validateBookingTimes(request.getStartTime(), request.getEndTime());
        checkDeskAvailability(request.getDeskId(), request.getStartTime(), request.getEndTime());
        checkUserAvailability(user.getId(), request.getStartTime(), request.getEndTime());

        Booking booking = Booking.builder()
                .user(user)
                .desk(desk)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        updateDeskStatus(desk, DeskStatus.DEACTIVATED);

        emailService.sendBookingConfirmationEmail(
                email,
                savedBooking.getId(),
                savedBooking.getDesk().getDeskName(),
                savedBooking.getDesk().getZone(),
                OffsetDateTime.now()
        );

        log.info("Booking created successfully with id: {} for user: {}", savedBooking.getId(), email);
        return bookingMapper.maptoDto(savedBooking);
    }

    public void cancelBooking(String email, Long id) {
        log.info("Attempting to cancel booking id: {} for user: {}", id, email);

        Booking booking = bookingRepository.findById(id).orElseThrow(() -> {
            log.error("Booking not found with id: {}", id);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_BOOKING_FOUND", "Cannot find booking id");
        });

        if (!booking.getUser().getEmail().equals(email)) {
            log.error("User {} attempted to cancel booking {} belonging to another user", email, id);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_CANCEL_BOOKING", "Cannot find user email");
        }

        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            log.error("Cannot cancel booking id: {} as it has already started", id);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_ALREADY_STARTED", "Cannot cancel booking that has already started");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        Desk desk = booking.getDesk();
        updateDeskStatus(desk, DeskStatus.ACTIVE);
        log.info("Booking id: {} cancelled successfully by user: {}", id, email);

        emailService.sendCancelledBookingEmail(
                email,
                booking.getId(),
                booking.getDesk().getDeskName(),
                booking.getDesk().getZone(),
                OffsetDateTime.now()
        );
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
        log.info("Booking id: {} deleted successfully", id);
    }

    private void updateDeskStatus(Desk desk, DeskStatus status) {
        log.debug("Updating desk id: {} status to {}", desk.getId(), status);
        desk.setStatus(status);
        deskRepository.save(desk);
        log.info("Desk id: {} status updated to {}", desk.getId(), status);
    }

    public void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime.isBefore(now)) {
            log.error("Invalid booking time: start time {} is before current time {}", startTime, now);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before today's date and time");
        }

        if (endTime.isBefore(startTime)) {
            log.error("Invalid booking time: end time {} is before start time {}", endTime, startTime);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "End time must be after start time");
        }

        long hours = Duration.between(startTime, endTime).toHours();

        if (hours > MAX_BOOKING_HOURS) {
            log.error("Booking duration {} hours exceeds maximum of {} hours", hours, MAX_BOOKING_HOURS);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking more than 8 Hours");
        }

        if (hours < MIN_BOOKING_HOURS) {
            log.error("Booking duration {} hours is less than minimum of {} hour", hours, MIN_BOOKING_HOURS);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking less than 1 Hour");
        }

        log.debug("Booking times validated successfully. Duration: {} hours", hours);
    }

    public void checkDeskAvailability(Long desk_id, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Checking desk availability for desk id: {} between {} and {}", desk_id, startTime, endTime);

        List<Booking> overLappingBookings = bookingRepository.findOverlappingBookings(
                desk_id,
                startTime,
                endTime
        );

        if (!overLappingBookings.isEmpty()) {
            log.error("Desk id: {} has {} overlapping bookings for the requested time period", desk_id, overLappingBookings.size());
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "DESK_NOT_AVAILABLE", "Desk is not available for booking due to overlapping time");
        }

        log.debug("Desk id: {} is available for booking", desk_id);
    }

    public void checkUserAvailability(Long user_id, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Checking user availability for user id: {} between {} and {}", user_id, startTime, endTime);

        List<Booking> userBookings = bookingRepository.findUserBookings(
                user_id,
                startTime,
                endTime
        );

        if (!userBookings.isEmpty()) {
            log.error("User id: {} already has {} booking(s) in the requested time period", user_id, userBookings.size());
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_NOT_AVAILABLE", "Already have a booking in this time period");
        }

        log.debug("User id: {} is available for booking", user_id);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(String email) {

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User does not exist"));

        List<Booking> bookings = bookingRepository.findBookingsByUserOrderByStartTimeDesc(user);

        return bookings.stream().
                map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> getUpcomingBookings(String email) {
        log.info("Fetching upcoming bookings for user: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user");
        });

        List<Booking> bookings = bookingRepository.findUpcomingBookingsByUserId(
                user.getId(),
                LocalDateTime.now()
        );

        log.info("Found {} upcoming bookings for user: {}", bookings.size(), email);
        return bookings.stream()
                .map(bookingMapper::maptoDto)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getUpcomingBookingsR(String email) {
        log.info("Fetching upcoming bookings for user: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user");
        });

        List<Booking> bookings = bookingRepository.findUpcomingBookingsWithin8Hours(
                user.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(8)
        );

        log.info("Found {} upcoming bookings for user: {}", bookings.size(), email);
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
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

}