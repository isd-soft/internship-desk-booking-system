package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.*;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.AdminService;
import com.project.internship_desk_booking_system.service.BookingService;
import com.project.internship_desk_booking_system.service.DeskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller exposing administrative operations for desks, bookings, zones, and users.
 * <p>
 * This controller is restricted to users  with the {@PreAuthorize ADMIN} role and allows:
 * <ul>
 *     <li>Desk management (create, edit, activate, deactivate, delete, restore)</li>
 *     <li>Desk coordinates updates and layout synchronization</li>
 *     <li>Booking management (cancel, edit)</li>
 *     <li>User role management</li>
 *     <li>Retrieval of zones, statuses, and enum-based metadata</li>
 *     <li>Fetching registered users for reporting</li>
 * </ul>
 * <p>
 * All business logic is delegated to {@link AdminService}, {@link DeskService},
 * and {@link BookingService}.
 *
 * <p><b>Base URL:</b> {@code /api/v1/admin}</p>
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final DeskService deskService;
    private final BookingService bookingService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/desk/{id}")
    public ResponseEntity<DeskDto> getDeskById(
            @PathVariable("id") Long deskId
    ) {
        return ResponseEntity.ok(
                adminService.getDeskById(deskId));
    }

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
    public ResponseEntity<List<DeskDto>> getAllDesks() {
        return ResponseEntity.ok(adminService.getAllDesks());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/desk/{id}")
    public ResponseEntity<Void> deleteDesk(
            @PathVariable("id") Long deskId,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        adminService.deleteDesk(deskId, reason);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("cancel/booking/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable("id") Long bookingId,
            @RequestBody CancelBookingDTO request
    ){
        return ResponseEntity
                .ok(adminService.cancelBooking(bookingId, request.getReason()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("edit/booking/{id}")
    public ResponseEntity<BookingResponse> editBooking(
            @PathVariable("id") Long bookingId,
            @RequestBody BookingUpdateCommand bookingUpdateCommand
    ) {
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
    ) {
        return ResponseEntity.ok(
                adminService.changeCurrentCoordinates(coordinatesUpdateCommand)
        );
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/desks/deleted")
    public ResponseEntity<List<DeskDto>> getDeletedDesks() {
        return ResponseEntity.ok(adminService.getAllDeletedDesks());
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/restore/desk/{id}")
    public ResponseEntity<Void> restoreDesk(
            @PathVariable("id") Long deskId
    ) {
        adminService.restoreDesk(deskId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/emails")
    public ResponseEntity<List<EmailRoleDTO>> getAllUserEmails() {
        log.info("Admin request to fetch all registered user emails and roles");
        return ResponseEntity.ok(adminService.getAllRegisteredUserEmails());
    }

    @GetMapping("/desk-status")
    public ResponseEntity<List<String>> getDeskStatusEnum() {
        return ResponseEntity.ok(deskService.getAllStatusDeskEnum());
    }

    @GetMapping("/desk-type")
    public ResponseEntity<List<String>> getDeskTypeEnum() {
        return ResponseEntity.ok(deskService.getAllTypeDeskEnum());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/desks/restoreCoordinates")
    public ResponseEntity<Void> restoreCoordinates() {
        adminService.restoreCoordinates();
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/desks/saveAll")
    public ResponseEntity<Integer> saveAll(
            @RequestBody List<DeskDto> updates
    ) {
        return ResponseEntity.ok(
                adminService.saveAllDesks(updates)
        );
    }

    @GetMapping("/booking-status")
    public ResponseEntity<List<String>> getBookingStatusEnum() {
        return ResponseEntity.ok(bookingService.getBookingStatusEnum());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/zones")
    public ResponseEntity<List<ZoneDto>> getAllZones() {
        return ResponseEntity.ok(adminService.getAllZones());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/images")
    public ResponseEntity<List<ImageDto>> getAllImages() {
        return ResponseEntity
                .ok(adminService.getAllImages());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/images/upload")
    public ResponseEntity<Void> uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        adminService.uploadImage(file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/role")
    public ResponseEntity<EmailRoleDTO> updateUserRole(
            @RequestBody @Valid EmailRoleDTO dto, @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        log.info("Admin request to change user role: {}", dto);
        return ResponseEntity.ok(adminService.updateUserRole(dto, principal));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/images/setBackground/{id}")
    public ResponseEntity<byte[]> setBackgroundImage(
            @PathVariable("id") Long id
    ) {
        adminService.setBackgroundImage(id);

        return ResponseEntity
                .ok()
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/images/delete/{id}")
    public ResponseEntity<byte[]> deleteImage (
            @PathVariable("id") Long id
    ) {
        adminService.deleteImage(id);
        return ResponseEntity
                .ok()
                .build();
    }
}
