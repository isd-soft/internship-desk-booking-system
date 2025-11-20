package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceValidation {


    public void validateNotUpdatingSelf(String currentEmail, String targetEmail) {
        if (currentEmail.equalsIgnoreCase(targetEmail)) {
            throw new ExceptionResponse(HttpStatus.CONFLICT,"UPDATING_YOURSELF","You cannot change your own role");
        }
    }

    public void validateIsAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ExceptionResponse(HttpStatus.CONFLICT,"IS_ADMIN","You cannot change your role");
        }
    }
}
