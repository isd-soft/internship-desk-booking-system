package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.CreateBookingCommand;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.service.FavouriteDesksService;
import com.project.internship_desk_booking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DeskRepository deskRepository;
    private final FavouriteDesksService favouriteDeskService;

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
        public ResponseEntity<?> addFavourite(Authentication authentication, @PathVariable Long deskId) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            favouriteDeskService.addFavouriteDesk(userId, deskId);
            return ResponseEntity.ok("Desk added to favourites");
        }

        @DeleteMapping("/favourite/remove/{deskId}")
        public ResponseEntity<?> removeFavourite(Authentication authentication, @PathVariable Long deskId) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            favouriteDeskService.removeFavouriteDesk(userId, deskId);
            return ResponseEntity.ok("Desk removed from favourites");
        }

        @GetMapping
        public ResponseEntity<List<FavouriteDesksDTO>> getFavourites(Authentication authentication) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();

            List<Desk> favourites = favouriteDeskService.getFavouriteDesks(userId);

            List<FavouriteDesksDTO> response = favourites.stream()
                    .map(desk -> new FavouriteDesksDTO(
                            desk.getId(),
                            desk.getDeskName(),
                            desk.getZone(),
                            true
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        }
    }
}
