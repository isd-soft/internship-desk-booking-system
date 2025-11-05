package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/bookings")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok().body(bookingService.getUserBookings(principal.getEmail()));
    }

}
