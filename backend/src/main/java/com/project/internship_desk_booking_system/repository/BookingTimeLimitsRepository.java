package com.project.internship_desk_booking_system.repository;
import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

    public interface BookingTimeLimitsRepository extends JpaRepository<BookingTimeLimits, Long> {

        @Query("SELECT btl FROM BookingTimeLimits btl WHERE btl.isActive = true")
        Optional<BookingTimeLimits> findActivePolicy();

        boolean existsByIsActive(Boolean isActive);
    }
