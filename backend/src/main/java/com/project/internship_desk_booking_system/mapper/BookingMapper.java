package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

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
    public BookingResponseDto maptoDto(Booking booking) {
        double durationHours = Duration.between(booking.getStartTime(), booking.getEndTime()).toHours();

        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .deskId(booking.getDesk().getId())
                .deskName(booking.getDesk().getDeskName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().name())
                .durationHours(durationHours)
                .build();
    }
}
