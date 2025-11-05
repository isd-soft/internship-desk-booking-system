package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(
            AdminService adminService
    ) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addDesk")
    public ResponseEntity<Desk> addDesk(
            @RequestBody @Valid DeskDTO deskDTO
    ) {
        return ResponseEntity
                .ok(adminService.addDesk(deskDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deactivateDesk/{id}")
    public ResponseEntity<Desk> deactivateDesk(
            @PathVariable("id") Long deskId
    ) {
        return ResponseEntity
                .ok(adminService.deactivateDesk(deskId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/edit/desk/{id}")
    public ResponseEntity<Desk> editDesk(
            @PathVariable("id") Long deskId,
            @RequestBody DeskUpdateDTO updates
    ) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(adminService.editDesk(deskId, updates));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/desks")
    public ResponseEntity<List<Desk>> getAllDesks(){
        return ResponseEntity.ok(adminService.getAllDesks());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/desk/{id}")
    public ResponseEntity<Void> deleteDesk(
            @PathVariable("id") Long deskId
    ) {
        adminService.deleteDesk(deskId);
        return ResponseEntity.ok().build();
    }
}
