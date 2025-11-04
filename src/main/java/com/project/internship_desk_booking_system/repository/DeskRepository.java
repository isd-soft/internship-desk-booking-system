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

    List<Desk> findByZoneAndType(String zone, DeskType type);

    List<Desk> findByZoneAndStatus(String zone, DeskStatus status);

    List<Desk> findByTypeAndStatus(DeskType type, DeskStatus status);

    List<Desk> findByZoneAndTypeAndStatus(String zone, DeskType type, DeskStatus status);

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

    @Query("SELECT d FROM Desk d WHERE d.status = 'ACTIVE' AND d.zone = :zone " +
            "AND (d.type = 'SHARED' " +
            "OR (d.type = 'ASSIGNED' AND d.isTemporarilyAvailable = true " +
            "AND d.temporaryAvailableFrom <= :dateTime " +
            "AND d.temporaryAvailableUntil >= :dateTime))")
    List<Desk> findAvailableDesksForBookingInZoneAt(@Param("zone") String zone,
                                                    @Param("dateTime") LocalDateTime dateTime);

    Optional<Desk> findByDeskName(String deskName);

    List<Desk> findByDeskNameContainingIgnoreCase(String keyword);

    boolean existsByDeskName(String deskName);

    long countByStatus(DeskStatus status);

    long countByZone(String zone);

    long countByType(DeskType type);

    long countByZoneAndStatus(String zone, DeskStatus status);

    @Query("SELECT DISTINCT d.zone FROM Desk d ORDER BY d.zone")
    List<String> findAllDistinctZones();
}