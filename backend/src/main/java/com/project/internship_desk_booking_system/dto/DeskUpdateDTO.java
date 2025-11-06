package com.project.internship_desk_booking_system.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;


import java.time.LocalDateTime;

public record DeskUpdateDTO(
        String deskName,
        String zone,
        @JsonProperty("type")
        DeskType deskType,
        @JsonProperty("status") DeskStatus deskStatus,
        Boolean isTemporarilyAvailable,
        LocalDateTime temporaryAvailableFrom,
        LocalDateTime temporaryAvailableUntil) { }
