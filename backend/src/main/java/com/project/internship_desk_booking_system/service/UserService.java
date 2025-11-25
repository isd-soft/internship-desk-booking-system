package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;

    @Transactional
    public void deleteUser(CustomUserPrincipal principal, Long userId) {
        User userToDelete = userValidationService.validateDeleteUser(principal, userId);
        userRepository.delete(userToDelete);
    }
}
