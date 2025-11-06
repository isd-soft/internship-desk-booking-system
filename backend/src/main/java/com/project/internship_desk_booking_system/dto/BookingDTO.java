package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingDTO(
        Long deskId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BookingStatus status
) {
}
