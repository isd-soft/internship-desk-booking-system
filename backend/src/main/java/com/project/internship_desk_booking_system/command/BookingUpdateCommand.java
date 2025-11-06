package com.project.internship_desk_booking_system.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.internship_desk_booking_system.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingUpdateCommand(
        Long userId,
        Long deskId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        @JsonProperty("status")
        BookingStatus status
){ }
