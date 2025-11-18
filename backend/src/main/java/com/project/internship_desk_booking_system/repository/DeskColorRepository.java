package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.DeskColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeskColorRepository extends JpaRepository<DeskColor, Long> {
    Optional<DeskColor> findByColorName(String colorName);

    boolean existsByColorName(String colorName);

    boolean existsByColorCode(String colorCode);

    boolean existsByColorMeaning(String colorMeaning);
}
