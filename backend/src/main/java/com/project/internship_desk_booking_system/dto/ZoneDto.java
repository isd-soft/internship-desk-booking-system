package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZoneDto {
    private Long id;
    private String zoneName;
    private String zoneAbv;
}
