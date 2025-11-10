package com.project.internship_desk_booking_system.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;


import java.time.LocalDateTime;

public record DeskUpdateDTO(
        String displayName,
        Long zone,
        @JsonProperty("type")
        DeskType type,
        @JsonProperty("status") DeskStatus deskStatus,
        Boolean isTemporarilyAvailable,
        LocalDateTime temporaryAvailableFrom,
        LocalDateTime temporaryAvailableUntil) { }
