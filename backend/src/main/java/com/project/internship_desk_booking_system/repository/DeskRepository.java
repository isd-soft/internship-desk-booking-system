package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
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

    List<Desk> findByType(DeskType type);
    List<Desk> findByStatus(DeskStatus status);

    @Query(value = """
            SELECT id, current_x, current_y
            FROM desk
            """,
            nativeQuery = true)
    List<DeskCoordinatesDTO> findCurrentCoordinates();

    @Query(value = """
            SELECT id, base_x, base_y
            FROM desk
            """,
            nativeQuery = true)
    List<DeskCoordinatesDTO> findBaseCoordinates();
}