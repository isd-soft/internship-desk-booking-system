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

    public void validateBookingLogic(User user, BookingCreateRequest request) {
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();
        validateBookingTimes(start, end);
        validateOfficeHours(start, end);
        validateMaxDaysInAdvance(start);
        checkDeskAvailability(request.getDeskId(), start, end);
        //checkUserAvailability(user.getId(), start, end);
        validateWeeklyHoursLimit(user.getId(), start, end);

        log.info("Validation passed for user {} desk {}", user.getEmail(), request.getDeskId());
    }

    public void validateDeskType(Desk desk, LocalDateTime start, LocalDateTime end) {

        if (desk.getType() == DeskType.SHARED) {
            return;
        }

        if (Boolean.TRUE.equals(desk.getIsTemporarilyAvailable())) {
            validateTemporaryWindow(desk, start, end);
            return;
        }

        throw new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "DESK_NOT_BOOKABLE",
                "This desk cannot be booked"
        );
    }


    private void validateTemporaryWindow(Desk desk, LocalDateTime start, LocalDateTime end) {

        LocalDateTime from = desk.getTemporaryAvailableFrom();
        LocalDateTime until = desk.getTemporaryAvailableUntil();

        if (from == null || until == null) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "TEMPORARY_WINDOW_INVALID",
                    "Temporary availability window is not configured for this desk"
            );
        }

        if (start.isBefore(from) || end.isAfter(until)) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "OUT_OF_TEMPORARY_RANGE",
                    "Desk is temporarily available only between " + from + " and " + until
            );
        }
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

    /*public void checkUserAvailability(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (bookingRepository.existsUserConflict(userId, startTime, endTime))
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_CONFLICT", "You already have a booking");
    }*/

    public BookingStatus resolveStatus(LocalDateTime start) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(start)) {
            return BookingStatus.SCHEDULED;
        }
        return BookingStatus.ACTIVE;
    }

}
