package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.BookingService;
import com.project.internship_desk_booking_system.service.FavouriteDesksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final BookingService bookingService;
    private final FavouriteDesksService favouriteDesksService;

    @PostMapping("/booking/reserve")
    public ResponseEntity<BookingResponseDto> createBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid BookingCreateRequest request
    ) {
        log.info("Creating booking for user: {}", principal.getEmail());

        BookingResponseDto booking = bookingService.createBooking(
                principal.getEmail(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(booking);
    }

    @DeleteMapping("/booking/cancel/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long bookingId
    ) {
        log.info("Cancelling booking id: {} for user: {}", bookingId, principal.getEmail());

        bookingService.cancelBooking(
                principal.getEmail(),
                bookingId
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long bookingId
    ) {
        log.info("Fetching booking id: {} for user: {}", bookingId, principal.getEmail());

        BookingResponseDto booking = bookingService.getBookingById(
                principal.getEmail(),
                bookingId
        );

        return ResponseEntity.ok(booking);
    }


}

