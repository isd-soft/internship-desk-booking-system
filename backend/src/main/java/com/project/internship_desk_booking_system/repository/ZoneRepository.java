package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findById(Long id);
}
