package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
