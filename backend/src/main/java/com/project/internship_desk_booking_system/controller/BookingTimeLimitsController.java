package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import com.project.internship_desk_booking_system.service.BookingTimeLimitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
