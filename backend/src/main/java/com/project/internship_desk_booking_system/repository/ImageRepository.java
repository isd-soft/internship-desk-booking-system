package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("""
            SELECT i FROM Image i
            WHERE i.isBackground = true"""
    )
    Optional<Image> findBackground();
}
