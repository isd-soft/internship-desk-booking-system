package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.CreateBookingCommand;
import com.project.internship_desk_booking_system.dto.BookingDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.exception.ExceptionResponse;
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
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;

    public BookingDTO createBooking(
            CustomUserPrincipal principal,
            CreateBookingCommand command
    ){
        User user = getUserByEmail(principal.getEmail());
        Desk desk = deskRepository
                .findById(command.deskId())
                .orElseThrow(
                        () -> new ExceptionResponse(
                                HttpStatus.NOT_FOUND,
                                "DESK_NOT_FOUND",
                                "Desk with id: " + command.deskId() + " not found")
                );

        if(command.startTime().isAfter(LocalDateTime.now().plusDays(20))){
            log.warn(
                    "Booking date {} - {} is more than 20 days for User {}",
                    command.startTime(),
                    command.endTime(),
                    principal.getEmail()
            );
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "DAYS_OUT_OF_BOUNDS",
                    "start time is after 20 days"
            );
        }

        Duration duration = Duration.between(command.startTime(), command.endTime());
        if(duration.toHours() < 1 || duration.toHours() > 8){
            log.warn(
                    "Booking duration {} {} - {} is more than 8 hours or less than 1 hour for User {}",
                    duration.toHours(),
                    command.startTime(),
                    command.endTime(),
                    principal.getEmail()
            );
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "HOURS_OUT_OF_BOUNDS",
                    "Selected date is less than 1 hour or more than 8 hours"
            );
        }

        if(!checkIfDeskIsFree(
                desk.getId(),
                command.startTime(),
                command.endTime())
        ){
            log.warn(
                    "For desk {} in time range {} - {} there is a booking already; User {}",
                    desk.getId(),
                    command.startTime(),
                    command.endTime(),
                    principal.getEmail()
            );
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "CROSSING_OF_BOOKING_DATES",
                    "There are at least 1 booking on the same date"
            );
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setDesk(desk);
        booking.setStartTime(command.startTime());
        booking.setEndTime(command.endTime());
        booking.setStatus(BookingStatus.ACTIVE);

        log.info(
                "Saving booking with : userId {}; deskId {}; status {}; startTime {} ; endTime {} ",
                booking.getUser().getId(),
                booking.getDesk().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus()
        );

        bookingRepository.save(booking);

        return new BookingDTO(
                desk.getId(),
                command.startTime(),
                command.endTime(),
                BookingStatus.ACTIVE);
    }

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

    private boolean checkIfDeskIsFree(
            Long deskId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        List<Booking> bookings = bookingRepository
                .findOverlappingBookings(
                        deskId,
                        startTime,
                        endTime
                );
        for(Booking booking : bookings){
            LocalDateTime existingStartTime = booking.getStartTime();
            LocalDateTime existingEndTime = booking.getEndTime();

            if(startTime.isBefore(existingEndTime) && existingStartTime.isBefore(endTime)){
                return false;
            }
        }
        return true;
    }

    public List<Desk> getAllDesks(){
        return deskRepository.findAll();
    }
}
