package com.project.internship_desk_booking_system.command;

import jakarta.validation.constraints.NotNull;

public record CoordinatesUpdateCommand(
        @NotNull
        Long deskId,
        @NotNull
        Double currentX,
        @NotNull
        Double currentY
) { }
