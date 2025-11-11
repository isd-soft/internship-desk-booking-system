package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT COUNT(DISTINCT b.user) FROM Booking b WHERE b.startTime > :startTime")
    long countUsersWithBookingsAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(DISTINCT b.user) FROM Booking b " +
            "WHERE b.startTime BETWEEN :startDate AND :endDate")
    long countUsersWithBookingsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
