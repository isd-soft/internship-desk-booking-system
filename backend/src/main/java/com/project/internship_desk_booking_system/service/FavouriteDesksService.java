package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.FavouriteDesks;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.FavouriteDeskMapper;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.FavouriteDesksRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavouriteDesksService {

    private final FavouriteDesksRepository favouriteDesksRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final FavouriteDeskMapper favouriteDeskMapper;
    private final EmailService emailService;

    @Transactional
    public void addFavouriteDesk(String email, Long deskId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        if (favouriteDesksRepository.findByUserIdAndDeskId(user.getId(), deskId).isPresent()) {
            return;
        }

        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK NOT_FOUND", "desk not found"));

        FavouriteDesks favourite = new FavouriteDesks(user, desk);
        favouriteDesksRepository.save(favourite);
    }


    @Transactional
    public void removeFavouriteDesk(String email, Long deskId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        favouriteDesksRepository.deleteByUserIdAndDeskId(user.getId(), deskId);
    }

    @Transactional(readOnly = true)
    public List<FavouriteDesksDTO> getFavouriteDesks(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        return favouriteDesksRepository.findByUser(user)
                .stream()
                .map(favouriteDeskMapper::toDto)
                .toList();
    }

    public void notifyUsersAboutDeskChanges(Desk desk, List<String> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        List<String> emails = favouriteDesksRepository.findEmailsByDeskId(desk.getId());

        emails.stream()
                .forEach(email ->
                        emailService.sendImportantDeskRelatedEmail(
                                email,
                                desk.getDeskName(),
                                changes
                        )
                );
    }

}
