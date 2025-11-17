package com.project.internship_desk_booking_system.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import java.time.LocalDateTime;

public record DeskDto(
        @Null
        Long id,
        @NotBlank
        String displayName,
        ZoneDto zoneDto,
        DeskType type,
        DeskStatus deskStatus,
        @JsonAlias({"is_temporary_available"})
        Boolean isTemporarilyAvailable,
        @Null
        LocalDateTime temporaryAvailableFrom,
        @Null
        LocalDateTime temporaryAvailableUntil,
        Double currentX,
        Double currentY,
        Double baseX,
        Double baseY,
        Long height,
        Long width
) {
}
