package com.project.internship_desk_booking_system.command;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
//
public record CreateBookingCommand(
        @NotNull
        Long deskId,
        @NotNull
        LocalDateTime startTime,
        @NotNull
        LocalDateTime endTime
){ }
