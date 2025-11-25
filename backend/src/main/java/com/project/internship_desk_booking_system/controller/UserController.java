package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.DeleteUserRequest;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping()
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserPrincipal principal, @RequestBody DeleteUserRequest deleteUserRequest) {
        userService.deleteUser(principal, deleteUserRequest.getEmail());
        return ResponseEntity.noContent().build();
    }
}
