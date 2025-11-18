package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final DeskMapper deskMapper;

    public BookingResponse toResponse(Booking booking) {
        DeskDto deskDto;

        if (booking.getDesk() == null) {
            deskDto = new DeskDto(
                    booking.getDeskId(),
                    "Deleted Desk",
                    null,
                    DeskType.UNAVAILABLE,
                    DeskStatus.DEACTIVATED,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } else {
            deskDto = deskMapper.toDto(booking.getDesk());
        }

        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                deskDto
        );
    }

    public BookingResponseDto maptoDto(Booking booking) {
        double durationHours = Duration.between(booking.getStartTime(), booking.getEndTime()).toHours();

        if (booking.getDesk() == null) {
            return BookingResponseDto.builder()
                    .id(booking.getId())
                    .userId(booking.getUser().getId())
                    .deskId(booking.getDeskId())
                    .deskName("Deleted Desk")
                    .deskType(DeskType.UNAVAILABLE)
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .status(booking.getStatus().name())
                    .durationHours(durationHours)
                    .build();
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .deskId(booking.getDesk().getId())
                .deskName(booking.getDesk().getDeskName())
                .deskType(booking.getDesk().getType())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().name())
                .durationHours(durationHours)
                .build();
    }
}