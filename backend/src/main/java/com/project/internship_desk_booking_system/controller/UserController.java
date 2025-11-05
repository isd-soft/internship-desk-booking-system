package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.CreateBookingCommand;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/booking/reserve")
    public ResponseEntity<BookingDTO> createBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid CreateBookingCommand command
    ) {
        return ResponseEntity
                .ok(userService.createBooking(principal, command));
    }

    @DeleteMapping("booking/remove")
    public ResponseEntity<String> removeBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam Long deskId
    ) {
        userService.deleteBooking(
                principal,
                deskId);

        return ResponseEntity
                .ok()
                .build();
    }
}
