package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final DeskMapper deskMapper;

    public BookingResponse toResponse(Booking booking) {

        return new BookingResponse(
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                deskMapper.toDto(booking.getDesk())
        );
    }
}
