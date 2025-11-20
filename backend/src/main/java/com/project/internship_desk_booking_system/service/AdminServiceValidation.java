package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceValidation {
    private final DeskRepository deskRepository;


    public void validateNotUpdatingSelf(String currentEmail, String targetEmail) {
        if (currentEmail.equalsIgnoreCase(targetEmail)) {
            throw new ExceptionResponse(HttpStatus.CONFLICT, "UPDATING_YOURSELF", "You cannot change your own role");
        }
    }

    public void validateIsAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ExceptionResponse(HttpStatus.CONFLICT, "IS_ADMIN", "You cannot change your role");
        }
    }

    public void validateDeskNameUniqueness(String currentName, String newName) {
        if (!currentName.equals(newName)) {
            if (deskRepository.existsByDeskName(newName)) {
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "DESK_NAME_EXISTS",
                        "Desk with name '" + newName + "' already exists"
                );
            }
        }
    }

    public void applyAutoDeactivationForType(Desk desk, DeskType newType) {
        if (newType == DeskType.UNAVAILABLE) {
            desk.setType(newType);
            desk.setStatus(DeskStatus.DEACTIVATED);
            desk.setIsTemporarilyAvailable(false);
            desk.setTemporaryAvailableFrom(null);
            desk.setTemporaryAvailableUntil(null);
        }
    }


}
