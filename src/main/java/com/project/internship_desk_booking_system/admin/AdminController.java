package com.project.internship_desk_booking_system.admin;

import com.project.internship_desk_booking_system.DTO.DeskDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(
          AdminService adminService
    ){
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addDesk")
    public ResponseEntity<Desk> addDesk(
            @RequestBody @Valid DeskDTO deskDTO
    ){
        return ResponseEntity
                .ok(adminService.addDesk(deskDTO));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deactivateDesk/{id}")
    public ResponseEntity<Desk> deactivateDesk(
            @PathVariable("id") Long deskId
    ){
        return ResponseEntity
                .ok(adminService.deactivateDesk(deskId));
    }

}
