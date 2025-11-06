package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getAllMyBookings(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getAllBookings(principal.getEmail()));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal, @Valid @RequestBody BookingCreateRequest bookingCreateRequest) {
        try {
            String email = principal.getEmail();
            BookingResponseDto booking = bookingService.createBooking(email, bookingCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingBookings(@AuthenticationPrincipal CustomUserPrincipal principal) {
        try {
            String email = principal.getEmail();
            List<BookingResponseDto> bookings = bookingService.getUpcomingBookings(email);
            return ResponseEntity.ok(bookings);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookedDesk/{bookingId}")
    public ResponseEntity<?> getBookingById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long bookingId) {
        try {
            String email = principal.getEmail();
            BookingResponseDto booking = bookingService.getBookingById(email, bookingId);
            return ResponseEntity.ok(booking);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/bookedDesk/{bookingId}")
    public ResponseEntity<?> cancelBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long bookingId) {
        try {
            String email = principal.getEmail();
            bookingService.cancelBooking(email, bookingId);
            return ResponseEntity.ok(createSuccessResponse());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Booking cancelled successfully");
        return response;
    }
}
