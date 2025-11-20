package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import com.project.internship_desk_booking_system.service.BookingTimeLimitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/**
 * REST controller responsible for managing booking time limit policies.
 * <p>
 * Provides administrative endpoints that allow:
 * <ul>
 *     <li>Retrieving the currently active booking time policy</li>
 *     <li>Updating global system constraints such as maximum days allowed in advance
 *          and maximum booking hours per week </li>
 * </ul>
 *
 * This controller is restricted to users with the {@PreAuthorize ADMIN} role and is used
 * to enforce business rules regarding how far in advance users can book (min-1, max-365 d)
 * and how many hours they may reserve within a given week (min-1, max-168 h).
 *
 * <p><b>Base URL:</b> {@code /api/v1/admin/booking-time-limits}</p>
 *
 * All business logic is delegated to {@link BookingTimeLimitsService},
 * data are stored in the DB.
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/booking-time-limits")
@RequiredArgsConstructor
public class BookingTimeLimitsController {

    private final BookingTimeLimitsService bookingTimeLimitsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<BookingTimeLimits> getActivePolicy() {
        try {
            BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();
            log.info("Successfully retrieved policy: {}", policy);
            return ResponseEntity.ok(policy);
        } catch (Exception e) {
            log.error("Error fetching policy", e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<BookingTimeLimits> updatePolicy(@RequestBody BookingTimeLimits request) {
        log.info("Admin request to update booking time limits policy");
        if (request.getMaxDaysInAdvance() == null || request.getMaxDaysInAdvance() < 1 || request.getMaxDaysInAdvance() > 365) {
            log.error("Invalid maxDaysInAdvance: {}", request.getMaxDaysInAdvance());
            throw new IllegalArgumentException("maxDaysInAdvance must be between 1 and 365");
        }

        if (request.getMaxHoursPerWeek() == null || request.getMaxHoursPerWeek() < 1 || request.getMaxHoursPerWeek() > 168) {
            log.error("Invalid maxHoursPerWeek: {}", request.getMaxHoursPerWeek());
            throw new IllegalArgumentException("maxHoursPerWeek must be between 1 and 168");
        }

        try {
            BookingTimeLimits updated = bookingTimeLimitsService.updatePolicy(request);
            log.info("Successfully updated policy: {}", updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating policy", e);
            throw e;
        }
    }
}
