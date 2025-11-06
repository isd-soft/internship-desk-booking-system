package com.project.internship_desk_booking_system.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import java.time.LocalDateTime;

public record DeskDTO(
        @Null
        Long id,
        @NotBlank
        String deskName,
        @NotBlank
        String zone,
        DeskType deskType,
        DeskStatus deskStatus,
        @JsonAlias({"is_temporary_available"})
        Boolean isTemporarilyAvailable,
        @Null
        LocalDateTime temporaryAvailableFrom,
        @Null
        LocalDateTime temporaryAvailableUntil,
        boolean isFavourite) { }

