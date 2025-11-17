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

        // Handle soft-deleted desks
        if (booking.getDesk() == null) {
            // Create a placeholder DTO for deleted desk
            deskDto = new DeskDto(
                    booking.getDeskId(),  // Use the deskId field
                    "Deleted Desk",
                    null,  // zoneDto
                    DeskType.UNAVAILABLE,
                    DeskStatus.DEACTIVATED,
                    false,  // isTemporarilyAvailable
                    null,   // temporaryAvailableFrom
                    null,   // temporaryAvailableUntil
                    null,   // currentX
                    null,   // currentY
                    null,   // baseX
                    null,
                    null,
                    null// baseY
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

        // Handle soft-deleted desks
        if (booking.getDesk() == null) {
            return BookingResponseDto.builder()
                    .id(booking.getId())
                    .userId(booking.getUser().getId())
                    .deskId(booking.getDeskId())  // Use the deskId field
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