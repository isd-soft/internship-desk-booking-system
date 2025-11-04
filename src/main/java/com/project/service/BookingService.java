package com.project.service;

import com.project.entities.Booking;
import com.project.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    public Booking CreateBooking(Booking booking){
        validateBookingDays();
    }

    //Validation for booking days in advance
    public void validateBookingDays(Booking booking, int N){
        LocalDate now = LocalDate.now();
        if(LocalDate.now().isAfter(now.plusDays(N))){
            throw new RuntimeException();
        }
    }
    // TO-DO: DACA O SA AM TIMP
    /*public void validateWeeklyHours(Booking booking, int M){
        LocalDate startOfWeek = booking.getStartTime().toLocalDate().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Booking> weeklyBookings;
    }*/
}
