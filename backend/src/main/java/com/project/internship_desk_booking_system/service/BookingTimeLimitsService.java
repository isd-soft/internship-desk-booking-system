package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingTimeLimitsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Service responsible for managing system-wide booking time limit policies by ADMIN role users.
 * <p>
 * A booking time limit policy defines global constraints for the booking feature,
 * including:
 * <ul>
 *     <li>The maximum number of days in advance a user is allowed to create a booking</li>
 *     <li>The maximum number of booking hours a user can accumulate per week</li>
 * </ul>
 *
 * The system is expected to maintain exactly one active policy at any time.
 * This service provides methods to retrieve the currently active policy
 * and to update its values stored in DB.
 *
 * <p>
 * All logic relies on {@link BookingTimeLimitsRepository} and maintains domain safety
 * using {@link ExceptionResponse} for error propagation and clear API feedback.
 * </p>
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingTimeLimitsService {

    private final BookingTimeLimitsRepository repository;

    @Transactional(readOnly = true)
    public BookingTimeLimits getActivePolicy() {
        log.info("Fetching active booking time limits policy");

        BookingTimeLimits policy = repository.findActivePolicy()
                .orElseThrow(() -> {
                    log.error("No active booking time limits policy found");
                    return new ExceptionResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "NO_ACTIVE_POLICY",
                            "System configuration error: no active policy found"
                    );
                });

        log.info("Found active policy: id={}, maxDays={}, maxHours={}",
                policy.getId(), policy.getMaxDaysInAdvance(), policy.getMaxHoursPerWeek());
        return policy;
    }
    /**
     * Updates the currently active booking time limits policy with the new values provided.
     * <p>
     * The system must always have exactly one active policy. This method loads that policy,
     * applies the updated limits, and saves the changes.
     *
     * @param request the new policy values (maxDaysInAdvance, maxHoursPerWeek)
     * @return the updated {@link BookingTimeLimits} entity
     * @throws ExceptionResponse if no active policy exists in the system
     */
    @Transactional
    public BookingTimeLimits updatePolicy(BookingTimeLimits request) {
        log.info("Updating booking time limits policy: maxDaysInAdvance={}, maxHoursPerWeek={}",
                request.getMaxDaysInAdvance(), request.getMaxHoursPerWeek());

        BookingTimeLimits policy = repository.findActivePolicy()
                .orElseThrow(() -> {
                    log.error("No active policy found to update");
                    return new ExceptionResponse(
                            HttpStatus.NOT_FOUND,
                            "NO_ACTIVE_POLICY",
                            "No active booking time limits policy found to update"
                    );
                });

        log.info("Found existing policy with id: {}", policy.getId());

        policy.setMaxDaysInAdvance(request.getMaxDaysInAdvance());
        policy.setMaxHoursPerWeek(request.getMaxHoursPerWeek());

        BookingTimeLimits savedPolicy = repository.save(policy);
        log.info("Policy updated successfully: id={}, maxDays={}, maxHours={}",
                savedPolicy.getId(), savedPolicy.getMaxDaysInAdvance(), savedPolicy.getMaxHoursPerWeek());

        return savedPolicy;
    }
}