package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingTimeLimitsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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