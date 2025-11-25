package com.project.internship_desk_booking_system.service;


import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestUserService {

    private final UserRepository userRepository;

    @Transactional
    public User createGuestUser() {
        User newGuest = new User(generateGuestEmail(), null);
        newGuest.setGuest(true);
        return userRepository.save(newGuest);
    }

    private String generateGuestEmail() {
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        return "guest_" + randomPart + "@guest.local";
    }
}
