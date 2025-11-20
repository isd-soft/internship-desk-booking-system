package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.dto.DColorDTO;
import com.project.internship_desk_booking_system.service.DeskColorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * REST controller responsible for managing desk color configurations used in the system.
 * <p>
 * Provides administrative endpoints that allow:
 * <ul>
 *     <li>Retrieving all available desk color definitions</li>
 *     <li>Fetching a specific desk color by its ID</li>
 *     <li>Creating new color entries</li>
 *     <li>Updating existing desk color configurations</li>
 *     <li>Deleting desk colors that are no longer needed</li>
 * </ul>
 *
 * This controller is accessible only to users  with the {@PreAuthorize ADMIN} role and is
 * used to configure the color metadata associated with desks, which can influence
 * UI display, categorization, or visual grouping within the booking system.
 *
 * <p><b>Base URL:</b> {@code /api/v1/admin/desk-colors}</p>
 *
 * All business logic is delegated to {@link DeskColorService},
 * data are stored in the DB.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/desk-colors")
@RequiredArgsConstructor
public class DeskColorController {

    private final DeskColorService deskColorService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DColorDTO>> getAllColors() {
        log.info("Admin request to fetch all desk colors");
        return ResponseEntity.ok(deskColorService.getAllColors());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<DColorDTO> getColorById(@PathVariable Long id) {
        log.info("Admin request to fetch desk color with id: {}", id);
        return ResponseEntity.ok(deskColorService.getColorById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DColorDTO> createColor(@RequestBody DColorDTO dto) {
        log.info("Admin request to create desk color: {}", dto.getColorName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deskColorService.createColor(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DColorDTO> updateColor(
            @PathVariable Long id,
            @RequestBody DColorDTO dto) {
        log.info("Admin request to update desk color id: {}", id);
        return ResponseEntity.ok(deskColorService.updateColor(id, dto));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Long id) {
        log.info("Admin request to delete desk color id: {}", id);
        deskColorService.deleteColor(id);
        return ResponseEntity.noContent().build();
    }
}