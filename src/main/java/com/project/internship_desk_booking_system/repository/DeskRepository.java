package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.Desk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeskRepository extends JpaRepository <Desk, Long> {
}
