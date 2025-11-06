package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouriteDesksService {

    private final FavouriteDesksRepository favouriteDesksRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addFavouriteDesk(String email, Long deskId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (favouriteDesksRepository.findByUserIdAndDeskId(user.getId(), deskId).isPresent()) {
            return;
        }

        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new IllegalArgumentException("Desk not found"));

        FavouriteDesks favourite = new FavouriteDesks(user, desk);
        favouriteDesksRepository.save(favourite);
    }


    @Transactional
    public void removeFavouriteDesk(String email, Long deskId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        favouriteDesksRepository.deleteByUserIdAndDeskId(user.getId(), deskId);
    }

    public List<FavouriteDesksDTO> getFavouriteDesksDTO(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<FavouriteDesks> favourites = favouriteDesksRepository.findByUser(user);

        return favourites.stream()
                .map(fav -> new FavouriteDesksDTO(
                        fav.getDesk().getId(),
                        fav.getDesk().getDeskName(),
                        fav.getDesk().getZone(),
                        true))
                .collect(Collectors.toList());
    }

    public List<FavouriteDesksDTO> getAllDesksWithFavourites(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Desk> allDesks = deskRepository.findAll();
        List<FavouriteDesks> favourites = favouriteDesksRepository.findByUser(user);

        Set<Long> favouriteDeskIds = favourites.stream()
                .map(fav -> fav.getDesk().getId())
                .collect(Collectors.toSet());

        return allDesks.stream()
                .map(desk -> new FavouriteDesksDTO(
                        desk.getId(),
                        desk.getDeskName(),
                        desk.getZone(),
                        favouriteDeskIds.contains(desk.getId())))
                .collect(Collectors.toList());
    }
}
