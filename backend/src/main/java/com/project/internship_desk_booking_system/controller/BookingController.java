package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getAllMyBookings(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getUserBookings(principal.getEmail()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public ResponseEntity<Void> createBooking(@AuthenticationPrincipal CustomUserPrincipal principal, @Valid @RequestBody BookingCreateRequest request) {
        bookingService.createBooking(principal.getEmail(), request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getUpcomingBookings(principal.getEmail()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok().body(bookingService.getBookingById(principal.getEmail(), bookingId));

    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long id) {
        bookingService.cancelBooking(principal.getEmail(), id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/byDate")
    public ResponseEntity<List<BookingDTO>> getBookingsByDate(
            @RequestParam LocalDate localDate
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByDate(localDate)
        );
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my/byDate")
    public ResponseEntity<List<BookingResponse>> getAllMyBookingsByDate(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam LocalDate localDate
    ) {
        return ResponseEntity.ok(bookingService
                .getAllUserBookingsByDate(
                        principal.getEmail(),
                        localDate
                )
        );
    }
}

