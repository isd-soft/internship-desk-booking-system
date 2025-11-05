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
    public void addFavouriteDesk(Long userId, Long deskId) {
        if (favouriteDesksRepository.findByUserIdAndDeskId(userId, deskId).isPresent()) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new IllegalArgumentException("Desk not found"));

        FavouriteDesks favourite = new FavouriteDesks(user, desk);
        favouriteDesksRepository.save(favourite);
    }

    @Transactional
    public void removeFavouriteDesk(Long userId, Long deskId) {
        favouriteDesksRepository.deleteByUserIdAndDeskId(userId, deskId);
    }
    public List<FavouriteDesksDTO> getAllDesksWithFavourites(Long userId) {
        List<Desk> allDesks = deskRepository.findAll();
        List<Desk> favouriteDesks = getFavouriteDesks(userId);

        Set<Long> favouriteIds = favouriteDesks.stream()
                .map(Desk::getId)
                .collect(Collectors.toSet());

        return allDesks.stream()
                .map(desk -> new FavouriteDesksDTO(
                        desk.getId(),
                        desk.getDeskName(),
                        desk.getZone(),
                        favouriteIds.contains(desk.getId())
                ))
                .collect(Collectors.toList());
    }

    public List<FavouriteDesksDTO> getFavouriteDesksDTO(Long userId) {
        return favouriteDesksRepository.findByUser(
                        userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                ).stream()
                .map(FavouriteDesks::getDesk)
                .sorted(Comparator.comparing(Desk::getDeskName, String.CASE_INSENSITIVE_ORDER))
                .map(desk -> new FavouriteDesksDTO(
                        desk.getId(),
                        desk.getDeskName(),
                        desk.getZone(),
                        true
                ))
                .collect(Collectors.toList());
    }

    private List<Desk> getFavouriteDesks(Long userId) {
        return favouriteDesksRepository.findByUser(
                        userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                ).stream()
                .map(FavouriteDesks::getDesk)
                .collect(Collectors.toList());
    }
}
