package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<DeskDto> addDesk(
            @RequestBody @Valid DeskDto deskDTO
    ) {
        return ResponseEntity
                .ok(adminService.addDesk(deskDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deactivateDesk/{id}")
    public ResponseEntity<DeskDto> deactivateDesk(
            @PathVariable("id") Long deskId
    ) {
        return ResponseEntity
                .ok(adminService.deactivateDesk(deskId));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/activateDesk/{id}")
    public ResponseEntity<DeskDto> activateDesk(
            @PathVariable("id") Long deskId
    ) {

        return ResponseEntity
                .ok(adminService.activateDesk(deskId));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/edit/desk/{id}")
    public ResponseEntity<DeskDto> editDesk(
            @PathVariable("id") Long deskId,
            @RequestBody DeskUpdateDTO updates
    ) {
        return ResponseEntity
                .ok(adminService.editDesk(deskId, updates));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/desks")
    public ResponseEntity<List<DeskDto>> getAllDesks(){
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/baseCoordinates")
    public ResponseEntity<List<DeskCoordinatesDTO>> getBaseCoordinates() {
        return ResponseEntity.ok(adminService.getBaseCoordinates());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/edit/currentCoordinates")
    public ResponseEntity<DeskCoordinatesDTO> editCurrentCoordinates(
            @RequestBody @Valid CoordinatesUpdateCommand coordinatesUpdateCommand
            ){
        return ResponseEntity.ok(
                adminService.changeCurrentCoordinates(coordinatesUpdateCommand)
        );
    }

}
