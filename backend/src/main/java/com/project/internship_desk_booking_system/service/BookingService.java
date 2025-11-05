package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.dto.BookingResponse;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.exception.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    // TODO: DACA O SA AM TIMP (optional)
    /*//Validation for booking days in advance
    public void validateBookingDays(Booking booking){
        LocalDate now = LocalDate.now();
        if(LocalDate.now().isAfter(now.plusDays(6))){
            throw new RuntimeException();
        }
    }*/
    // TODO: DACA O SA AM TIMP (optional)
    /*public void validateWeeklyHours(Booking booking, int M){
        LocalDate startOfWeek = booking.getStartTime().toLocalDate().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Booking> weeklyBookings;
    }*/


//    public List<BookingResponse> getUserBookings(String email) {
//        List<Booking> bookings = bookingRepository.findAllByUserEmail(email).orElseThrow((() -> new ExceptionResponse(HttpStatus.NOT_FOUND,"USER_NOT_FOUND","user not found");
//        return bookings.stream()
//                .map(bookingMapper::toResponse)
//                .toList();
//    }

}
