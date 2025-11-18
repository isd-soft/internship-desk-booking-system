package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.dto.DeskAvailabilityResponse;
import com.project.internship_desk_booking_system.dto.DeskColorDTO;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.service.BookingAvailabilityService;
import com.project.internship_desk_booking_system.service.DeskService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/desk")
@RequiredArgsConstructor
public class DeskController {

    private final DeskService deskService;
    private final BookingAvailabilityService bookingAvailabilityService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{deskId}/availability")
    public ResponseEntity<DeskAvailabilityResponse> getDeskAvailability(@PathVariable Long deskId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok().body(bookingAvailabilityService.getDeskAvailability(deskId, date));
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/coordinates")
    public ResponseEntity<List<DeskCoordinatesDTO>> getCoordinates() {
        return ResponseEntity.ok(deskService.getCoordinates());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/gray")
    public ResponseEntity<List<DeskColorDTO>> getGrayDesks(
            @RequestParam("localDate") LocalDate localDate
    ) {
        return ResponseEntity
                .ok(deskService.getGrayDesks(localDate));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/blue")
    public ResponseEntity<List<DeskColorDTO>> getBlueDesks(
            @RequestParam("localDate") LocalDate localDate
    ) {
        return ResponseEntity
                .ok(deskService.getBlueDesks(localDate));
    }
}
