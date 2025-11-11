package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.CreateBookingCommand;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;


    @Transactional
    public void deleteBooking(
            CustomUserPrincipal principal,
            Long deskId
    ){
        User user = getUserByEmail(principal.getEmail());
        log.info(
                "Deleting booking for userId {} deskId {}",
                user.getId(),
                deskId
                );
        bookingRepository.deleteBookingByDeskId(
                deskId,
                user.getId()
        );
    }

    private User getUserByEmail(
            String email
    ){
        log.info(
                "Getting User from database  by email {}",
                email
        );

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                        () -> new ExceptionResponse(HttpStatus.NOT_FOUND,
                                "USER_NOT_FOUND_BY_EMAIL"
                                ,"There is no user with such email"
                        )
                );
    }
}
