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

/**
 * Service layer responsible for managing user's favourite desks.
 *
 * This service provides methods to:
 * - Add a desk to a user's favourites
 * - Remove a desk from a user's favourites
 * - Retrieve all favourite desks for a specific user
 * - Notify users about changes to their favourite desks
 *
 * The service ensures that users and desks exist before performing operations
 * and uses FavouriteDeskMapper to convert entities into DTOs for API responses.
 */
@Service
@RequiredArgsConstructor
public class FavouriteDesksService {

    private final FavouriteDesksRepository favouriteDesksRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final FavouriteDeskMapper favouriteDeskMapper;
    private final EmailService emailService;

    /**
     * Adds a desk to the favourites of a specific user.
     * If the desk is already in the favourites, the method does nothing.
     *
     * @param email the email of the user
     * @param deskId the identifier of the desk to add
     * @throws ExceptionResponse if the user or desk does not exist
     */
    @Transactional
    public void addFavouriteDesk(String email, Long deskId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        if (favouriteDesksRepository.findByUserIdAndDeskId(user.getId(), deskId).isPresent()) {
            return;
        }

        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "desk not found"));

        FavouriteDesks favourite = new FavouriteDesks(user, desk);
        favouriteDesksRepository.save(favourite);
    }

    /**
     * Removes a desk from the favourites of a specific user.
     *
     * @param email the email of the user
     * @param deskId the identifier of the desk to remove
     * @throws ExceptionResponse if the user does not exist
     */
    @Transactional
    public void removeFavouriteDesk(String email, Long deskId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        favouriteDesksRepository.deleteByUserIdAndDeskId(user.getId(), deskId);
    }

    /**
     * Retrieves all favourite desks of a specific user.
     *
     * @param email the email of the user
     * @return a list of FavouriteDesksDTO representing the user's favourite desks
     * @throws ExceptionResponse if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<FavouriteDesksDTO> getFavouriteDesks(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        return favouriteDesksRepository.findByUser(user)
                .stream()
                .map(favouriteDeskMapper::toDto)
                .toList();
    }

    /**
     * Sends notifications to users about important changes to a desk they have marked as favourite.
     *
     * @param desk the Desk entity that has changed
     * @param changes a list of changes to notify users about
     */
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
