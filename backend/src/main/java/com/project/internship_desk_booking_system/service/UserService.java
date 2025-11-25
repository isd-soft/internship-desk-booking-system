package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.UserInfoDto;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;

    @Transactional
    public void deleteUser(CustomUserPrincipal principal, String email) {
        User userToDelete = userValidationService.validateDeleteUser(principal, email);
        userRepository.delete(userToDelete);
    }

    @Transactional(readOnly = true)
    public UserInfoDto getCurrentUserInfo(CustomUserPrincipal principal) {
        User user = userRepository.findByEmailIgnoreCase(principal.getEmail())
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found"
                ));

        return new UserInfoDto(
                user.getEmail(),
                user.getRole()
        );
    }

}
