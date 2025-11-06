package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.CreateBookingCommand;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.FavouriteDesksService;
import com.project.internship_desk_booking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DeskRepository deskRepository;
    private final FavouriteDesksService favouriteDesksService;
    private final UserRepository userRepository;

    @PostMapping("/booking/reserve")
    public ResponseEntity<BookingDTO> createBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid CreateBookingCommand command
    ) {
        return ResponseEntity
                .ok(userService.createBooking(principal, command));
    }

    @DeleteMapping("booking/remove")
    public ResponseEntity<String> removeBooking(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam Long deskId
    ) {
        userService.deleteBooking(
                principal,
                deskId);

        return ResponseEntity
                .ok()
                .build();
    }


    @PostMapping("/favourite/add/{deskId}")
    public ResponseEntity<?> addFavourite(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long deskId) {

        String email = principal.getEmail();
        favouriteDesksService.addFavouriteDesk(email, deskId);

        return ResponseEntity.ok("Desk added to favourites");
    }

    @DeleteMapping("/favourite/delete/{deskId}")
    public ResponseEntity<?> removeFavourite(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long deskId) {

        String email = principal.getEmail();
        favouriteDesksService.removeFavouriteDesk(email, deskId);

        return ResponseEntity.ok("Desk removed from favourites");
    }

    @GetMapping("/desks")
    public ResponseEntity<List<FavouriteDesksDTO>> getAllDesksWithFavourites(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        String email = principal.getEmail();
        List<FavouriteDesksDTO> desks = favouriteDesksService.getAllDesksWithFavourites(email);

        return ResponseEntity.ok(desks);
    }

    @GetMapping("/favourites")
    public ResponseEntity<List<FavouriteDesksDTO>> getFavourites(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        String email = principal.getEmail();
        List<FavouriteDesksDTO> favourites = favouriteDesksService.getFavouriteDesksDTO(email);

        return ResponseEntity.ok(favourites);
    }
}

