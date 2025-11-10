package com.project.internship_desk_booking_system.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeskStatsDTO {
    private Long desk_id;
    private String deskName;
    private String zone;
    private Long bookingCount;
}
