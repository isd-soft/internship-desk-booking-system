package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class DeskStatsDTO {
    private Long desk_id;
    private String deskName;
    private String zone;
    private Long bookingCount;
}
