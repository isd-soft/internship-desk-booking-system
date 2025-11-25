package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.AdminCreateBookingRequest;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceValidation {
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final GuestUserService guestUserService;


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
        } else {
            desk.setType(newType);
        }
    }

    @Transactional
    public User resolveUser(AdminCreateBookingRequest req) {
        if (!req.isGuest()) {
            return userRepository.findByEmailIgnoreCase(req.getEmail())
                    .orElseThrow(() -> new ExceptionResponse(
                            HttpStatus.NOT_FOUND,
                            "USER_NOT_FOUND",
                            "User with the provided email was not found"
                    ));
        } else {
            return guestUserService.createGuestUser();
        }
    }
}
