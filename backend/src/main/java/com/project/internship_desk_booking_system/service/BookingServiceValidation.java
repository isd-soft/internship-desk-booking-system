package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.config.BookingProperties;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceValidation {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;
    private final BookingTimeLimitsService bookingTimeLimitsService;

    /**
     * Validates the business logic for booking creation: user, time, office hours, desk, conflicts, and weekly limits.
     * Throws ExceptionResponse if any validation fails.
     * @param user the user making the booking
     * @param request the booking creation request
     */
    public void validateBookingLogic(User user, BookingCreateRequest request) {
        log.info("Validating booking logic for user {} and desk {}.", user.getEmail(), request.getDeskId());
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();
        validateUserOtherThanAdmin(user);
        validateBookingTimes(start, end);
        validateOfficeHours(start, end);
        validateMaxDaysInAdvance(start);
        checkDeskAvailability(request.getDeskId(), start, end);
        checkUserAvailability(user.getId(), start, end);
        validateWeeklyHoursLimit(user.getId(), start, end);
        log.info("Booking logic validation passed for user {} and desk {}.", user.getEmail(), request.getDeskId());
    }

    /**
     * Checks that the user is not the system admin (id=1). Throws if so.
     * @param user the user to check
     */
    public void validateUserOtherThanAdmin(User user) {
        if (user.getId().equals(1L)) {
            log.warn("Booking attempt from system account (id=1).");
            throw new ExceptionResponse(
                    HttpStatus.FORBIDDEN,
                    "USER_CANNOT_BOOK",
                    "This user account is not allowed to create bookings"
            );
        }
    }


    /**
     * Validates the desk type and its temporary window if needed. Throws if desk is not bookable.
     * @param desk the desk to validate
     * @param start booking start time
     * @param end booking end time
     */
    public void validateDeskType(Desk desk, LocalDateTime start, LocalDateTime end) {
        log.debug("Checking desk type {} for booking.", desk.getId());
        if (desk.getType() == DeskType.SHARED) {
            return;
        }
        if (Boolean.TRUE.equals(desk.getIsTemporarilyAvailable())) {
            validateTemporaryWindow(desk, start, end);
            return;
        }
        log.warn("Desk {} is not bookable.", desk.getId());
        throw new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "DESK_NOT_BOOKABLE",
                "This desk cannot be booked"
        );
    }


    /**
     * Validates the temporary window for a desk. Throws if not configured or out of range.
     * @param desk the desk
     * @param start booking start time
     * @param end booking end time
     */
    public void validateTemporaryWindow(Desk desk, LocalDateTime start, LocalDateTime end) {
        LocalDateTime from = desk.getTemporaryAvailableFrom();
        LocalDateTime until = desk.getTemporaryAvailableUntil();
        if (from == null || until == null) {
            log.warn("Temporary window for desk {} is not configured.", desk.getId());
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "TEMPORARY_WINDOW_INVALID",
                    "This desk’s temporary availability is not configured."
            );
        }
        if (start.isBefore(from) || end.isAfter(until)) {
            log.warn("Booking attempt out of temporary window for desk {}: {} - {}.", desk.getId(), from, until);
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "OUT_OF_TEMPORARY_RANGE",
                    "Desk is temporarily available only between " + from + " and " + until
            );
        }
    }


    /**
     * Validates that booking is within office hours. Throws if not.
     * @param start booking start time
     * @param end booking end time
     */
    public void validateOfficeHours(LocalDateTime start, LocalDateTime end) {
        int officeStart = bookingProperties.getOfficeStartHour();
        int officeEnd = bookingProperties.getOfficeEndHour();
        if (start.getHour() < bookingProperties.getOfficeStartHour() || (end.getHour() > officeEnd ||
                (end.getHour() == officeEnd && end.getMinute() > 0))) {
            log.warn("Booking outside office hours: {}-{}.", start, end);
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "OUTSIDE_OFFICE_HOURS",
                    String.format("Booking must be within office hours (%02d:00–%02d:00)", officeStart, officeEnd)
            );
        }
        log.debug("Booking is within office hours ({}–{}).", officeStart, officeEnd);
    }

    /**
     * Validates booking times: not in the past, end after start, duration within allowed range.
     * @param startTime booking start time
     * @param endTime booking end time
     */
    public void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startTime.isBefore(now)) {
            log.warn("Booking attempt in the past: {}.", startTime);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "You can’t book a desk in the past.");
        }
        if (endTime.isBefore(startTime)) {
            log.warn("End time is before start time: {} < {}.", endTime, startTime);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "The end time must be later than the start time.");
        }
        long effectiveHours = effectiveHoursExcludingLunch(startTime, endTime);
        if (effectiveHours < bookingProperties.getMinHours()) {
            log.warn("Booking too short: {} hours.", effectiveHours);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "TOO_SHORT",
                    "Booking must be at least " + bookingProperties.getMinHours() + " hour (excluding lunch)");
        }
        if (effectiveHours > bookingProperties.getMaxHours()) {
            log.warn("Booking too long: {} hours.", effectiveHours);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "TOO_LONG",
                    "Cannot book more than " + bookingProperties.getMaxHours() + " hours (excluding lunch)");
        }
        log.debug("Booking time validation passed — {} hours.", effectiveHours);
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

    /**
     * Checks if the desk is already booked for the given time. Throws if so.
     * @param deskId desk id
     * @param start booking start time
     * @param end booking end time
     */
    private void checkDeskAvailability(Long deskId, LocalDateTime start, LocalDateTime end) {
        if (bookingRepository.existsOverlappingBooking(deskId, start, end)) {
            log.warn("Desk {} is already booked for the selected time.", deskId);
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "DESK_ALREADY_BOOKED",
                    "This desk is already booked for this time"
            );
        }
    }

    /**
     * Validates that the booking is not too far in advance. Throws if it exceeds the policy.
     * @param startTime booking start time
     */
    private void validateMaxDaysInAdvance(LocalDateTime startTime) {
        BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();
        LocalDateTime now = LocalDateTime.now();
        long daysInAdvance = ChronoUnit.DAYS.between(now.toLocalDate(), startTime.toLocalDate());
        if (daysInAdvance > policy.getMaxDaysInAdvance()) {
            log.error("Booking too far in advance: {} days (max {}).", daysInAdvance, policy.getMaxDaysInAdvance());
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "BOOKING_TOO_FAR_AHEAD",
                    String.format("Cannot book more than %d days in advance. You are trying to book %d days ahead.",
                            policy.getMaxDaysInAdvance(), daysInAdvance)
            );
        }
        log.debug("Advance days validation passed: {} (max {}).", daysInAdvance, policy.getMaxDaysInAdvance());
    }

    /**
     * Validates that the user does not exceed the weekly hours limit. Throws if exceeded.
     * @param userId user id
     * @param startTime booking start time
     * @param endTime booking end time
     */
    private void validateWeeklyHoursLimit(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();
        LocalDateTime weekStart = startTime.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);
        log.debug("Checking weekly hours limit for user {}: {} - {}.", userId, weekStart, weekEnd);
        List<Booking> weeklyBookings = bookingRepository.findUserBookings(
                userId, weekStart, weekEnd
        );
        long bookedHours = weeklyBookings.stream()
                .mapToLong(b -> Duration.between(b.getStartTime(), b.getEndTime()).toHours())
                .sum();
        long newBookingHours = Duration.between(startTime, endTime).toHours();
        long totalHours = bookedHours + newBookingHours;
        log.debug("User {} has {} hours booked, requesting {} more (total: {}).", userId, bookedHours, newBookingHours, totalHours);
        if (totalHours > policy.getMaxHoursPerWeek()) {
            log.error("User {} exceeds weekly limit: {} (max {}).", userId, totalHours, policy.getMaxHoursPerWeek());
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "WEEKLY_HOURS_EXCEEDED",
                    String.format("Cannot exceed %d hours per week. You have %d hours booked and are requesting %d more hours.",
                            policy.getMaxHoursPerWeek(), bookedHours, newBookingHours)
            );
        }
        log.debug("Weekly hours limit validation passed for user {}.", userId);
    }

    public void checkUserAvailability(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (bookingRepository.existsUserConflict(userId, startTime, endTime))
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_CONFLICT", "You already have another booking during this time.");
    }

    public BookingStatus resolveStatus(LocalDateTime start) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(start)) {
            return BookingStatus.SCHEDULED;
        }
        return BookingStatus.ACTIVE;
    }

}
