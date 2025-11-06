package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addDesk")
    public ResponseEntity<DeskDTO> addDesk(
            @RequestBody @Valid DeskDTO deskDTO
    ) {
        return ResponseEntity
                .ok(adminService.addDesk(deskDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deactivateDesk/{id}")
    public ResponseEntity<DeskDTO> deactivateDesk(
            @PathVariable("id") Long deskId
    ) {
        return ResponseEntity
                .ok(adminService.deactivateDesk(deskId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/edit/desk/{id}")
    public ResponseEntity<DeskDTO> editDesk(
            @PathVariable("id") Long deskId,
            @RequestBody DeskUpdateDTO updates
    ) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity
                .ok(adminService.editDesk(deskId, updates));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/desks")
    public ResponseEntity<List<DeskDTO>> getAllDesks(){
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

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("cancel/booking/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(
        @PathVariable("id") Long bookingId
    ){
        return ResponseEntity
                .ok(adminService.cancelBooking(bookingId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("edit/booking/{id}")
    public ResponseEntity<BookingResponse> editBooking(
        @PathVariable("id") Long bookingId,
        @RequestBody BookingUpdateCommand bookingUpdateCommand
    ){
        return ResponseEntity
                .ok(adminService
                        .editBooking(
                                bookingId,
                                bookingUpdateCommand
                        ));
    }
}
