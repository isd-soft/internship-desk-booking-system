package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.service.FavouriteDesksService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
public class FavouriteDesksController {
    private final FavouriteDesksService favouriteDesksService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/{deskId}")
    public ResponseEntity<Void> addFavourite(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long deskId) {
        favouriteDesksService.addFavouriteDesk(principal.getEmail(), deskId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{deskId}")
    public ResponseEntity<Void> removeFavourite(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long deskId) {
        favouriteDesksService.removeFavouriteDesk(principal.getEmail(), deskId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping()
    public ResponseEntity<List<FavouriteDesksDTO>> getAllFavouriteDesks(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(favouriteDesksService.getFavouriteDesks(principal.getEmail()));
    }
}