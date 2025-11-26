package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.Zone;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeskRepository extends JpaRepository<Desk, Long> {

    Optional<Desk> findById(Long id);

    List<Desk> findByType(DeskType type);


    boolean existsByDeskName(String deskName);

    @Query(value = "SELECT * FROM desk WHERE id = :id", nativeQuery = true)
    Optional<Desk> findByIdIncludingDeleted(@Param("id") Long id);

    @Query(value = "SELECT * FROM desk WHERE is_deleted = true", nativeQuery = true)
    List<Desk> findAllDeleted();
    List<Desk> findByStatus(DeskStatus status);

    @Query("""
            SELECT new com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO(d.id, d.deskName, d.currentX, d.currentY, d.height, d. width)
            FROM Desk d
            """)
    List<DeskCoordinatesDTO> findCurrentCoordinates();

    @Query("""
            SELECT new com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO(d.id, d.deskName, d.baseX, d.baseY, d.height, d. width)
            FROM Desk d
            """)
    List<DeskCoordinatesDTO> findBaseCoordinates();

    @Modifying
    @Query("""
            UPDATE Desk d SET
            d.currentX = d.baseX,
            d.currentY = d.baseY
            WHERE isDeleted = false
            """)
    void restoreCoordinates();



}