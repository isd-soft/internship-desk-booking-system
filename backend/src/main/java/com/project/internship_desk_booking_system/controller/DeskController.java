package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.service.DeskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/desk")
@RequiredArgsConstructor
public class DeskController {

    private final DeskService deskService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/available")
    public ResponseEntity<List<DeskDto>> getAvailable() {
        return ResponseEntity.ok(deskService.getAllAvailableDesks());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("unavailable")
    public ResponseEntity<List<DeskDto>> getUnavailable() {
        return ResponseEntity.ok(deskService.getAllUnavailableDesks());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/coordinates")
    public ResponseEntity<List<DeskCoordinatesDTO>> getCoordinates() {
        return ResponseEntity.ok(deskService.getCoordinates());
    }

}
