package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
