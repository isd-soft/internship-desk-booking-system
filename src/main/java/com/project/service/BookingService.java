package com.project.service;

import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public

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
}
