package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.BookingResponse;
import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        DeskDTO deskDTO = null;
        Desk desk = booking.getDesk();

        if (desk != null) {
            deskDTO = new DeskDTO(
                    desk.getId(),
                    desk.getDeskName(),
                    desk.getZone(),
                    desk.getType(),
                    desk.getStatus(),
                    desk.getIsTemporarilyAvailable(),
                    desk.getTemporaryAvailableFrom(),
                    desk.getTemporaryAvailableUntil()
            );
        }

        return new BookingResponse(
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                deskDTO
        );
    }
}
