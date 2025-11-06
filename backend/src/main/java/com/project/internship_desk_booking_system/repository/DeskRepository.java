package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeskRepository extends JpaRepository<Desk, Long> {

    List<Desk> findByStatus(DeskStatus status);

    List<Desk> findByType(DeskType type);

    List<Desk> findByZone(String zone);

    List<Desk> findByIsTemporarilyAvailable(Boolean isTemporarilyAvailable);

    @Query("SELECT d FROM Desk d WHERE d.isTemporarilyAvailable = true " +
            "AND d.temporaryAvailableFrom <= :dateTime " +
            "AND d.temporaryAvailableUntil >= :dateTime " +
            "AND d.status = 'ACTIVE'")
    List<Desk> findTemporarilyAvailableDesksAt(@Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT d FROM Desk d WHERE d.status = 'ACTIVE' " +
            "AND (d.type = 'SHARED' " +
            "OR (d.type = 'ASSIGNED' AND d.isTemporarilyAvailable = true " +
            "AND d.temporaryAvailableFrom <= :dateTime " +
            "AND d.temporaryAvailableUntil >= :dateTime))")
    List<Desk> findAvailableDesksForBookingAt(@Param("dateTime") LocalDateTime dateTime);

    List<Desk> findByDeskNameContainingIgnoreCase(String keyword);

    boolean existsByDeskName(String deskName);

    long countByStatus(DeskStatus status);

    long countByZone(String zone);

    long countByType(DeskType type);

    @Query("SELECT DISTINCT d.zone FROM Desk d ORDER BY d.zone")
    List<String> findAllDistinctZones();
}