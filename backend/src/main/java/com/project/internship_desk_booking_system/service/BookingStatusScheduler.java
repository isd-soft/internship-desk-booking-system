package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingRepository bookingRepository;

    /**
     * Scheduled task that updates booking statuses in the database every 30 minutes.
     * Activates and confirms bookings based on the current time.
     */
    @Transactional()
    @Scheduled(fixedRateString = "${scheduler.interval}")
    public void updateBookingStatuses() {
        LocalDateTime now = LocalDateTime.now();

        int activated = bookingRepository.activateBookings(now);

        int confirmed = bookingRepository.confirmBookings(now);

        if (activated > 0 || confirmed > 0) {
            log.info("Scheduler updated statuses: ACTIVATED={}, CONFIRMED={}", activated, confirmed);
        }

    }
}
