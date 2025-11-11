package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.entity.Zone;
import lombok.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
public class DeskStatsDTO {
    private String deskName;
    private Long bookingCount;

    public DeskStatsDTO() {}

    public DeskStatsDTO(String deskName, Number bookingCount) {
        this.deskName = deskName;
        this.bookingCount = bookingCount == null ? 0L : bookingCount.longValue();
    }
}
