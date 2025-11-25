package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    @Value("${app.default-admin-id}")
    private Long mainAdminId;
    private final UserRepository userRepository;

    @Transactional
    public User validateDeleteUser(CustomUserPrincipal principal, Long userIdToDelete) {

        User admin = getCurrentAdmin(principal);
        User userToDelete = findTargetUser(userIdToDelete);

        validateNotSelfDelete(admin, userToDelete);
        validateNotMainAdmin(userToDelete);

        return userToDelete;
    }

    private User getCurrentAdmin(CustomUserPrincipal principal) {
        User user = userRepository.findByEmailIgnoreCase(principal.getEmail()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ExceptionResponse(HttpStatus.FORBIDDEN, "NOT_ADMIN", "Only admin users can perform this action");
        }

        return user;
    }

    private User findTargetUser(Long userIdToDelete) {
        return userRepository.findById(userIdToDelete).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User to delete not found"));
    }

    private void validateNotSelfDelete(User admin, User userToDelete) {
        if (admin.getId().equals(userToDelete.getId())) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "CANNOT_DELETE_SELF", "You cannot delete yourself");
        }
    }

    private void validateNotMainAdmin(User userToDelete) {
        if (Objects.equals(userToDelete.getId(), mainAdminId)) {
            throw new ExceptionResponse(HttpStatus.FORBIDDEN, "CANNOT_DELETE_MAIN_ADMIN", "This admin cannot be deleted");
        }
    }
}

