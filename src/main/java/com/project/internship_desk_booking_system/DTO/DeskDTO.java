package com.project.internship_desk_booking_system.DTO;

import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.LocalDateTime;

public record DeskDTO(
        @Null
        Long id,
        @NotBlank
        String deskName,
        @NotNull
        Integer floor,
        @NotBlank
        String zone,
        DeskType deskType,
        DeskStatus deskStatus,
        @Null
        Boolean isTemporarilyAvailable,
        @Null
        LocalDateTime temporaryAvailableFrom,
        @Null
        LocalDateTime temporaryAvailableUntil) { }
