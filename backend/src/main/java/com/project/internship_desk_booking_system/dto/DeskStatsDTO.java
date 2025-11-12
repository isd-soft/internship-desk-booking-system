package com.project.internship_desk_booking_system.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeskStatsDTO {
    private String deskName;
    private Long bookingCount;

    public DeskStatsDTO(String deskName, Number bookingCount) {
        this.deskName = deskName;
        this.bookingCount = bookingCount == null ? 0L : bookingCount.longValue();
    }
}