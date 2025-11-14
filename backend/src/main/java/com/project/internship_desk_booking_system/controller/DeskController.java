package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.dto.DeskColorDTO;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.service.DeskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    @GetMapping("/unavailable")
    public ResponseEntity<List<DeskDto>> getUnavailable() {
        return ResponseEntity.ok(deskService.getAllUnavailableDesks());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/coordinates")
    public ResponseEntity<List<DeskCoordinatesDTO>> getCoordinates() {
        return ResponseEntity.ok(deskService.getCoordinates());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/gray")
    public ResponseEntity<List<DeskColorDTO>> getGrayDesks(
            @RequestParam("localDate")LocalDate localDate
            ){
        return ResponseEntity
                .ok(deskService.getGrayDesks(localDate));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/blue")
    public ResponseEntity<List<DeskColorDTO>> getBlueDesks(
            @RequestParam("localDate") LocalDate localDate
    ){
        return ResponseEntity
                .ok(deskService.getBlueDesks(localDate));
    }
}
