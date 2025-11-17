package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DeskAvailabilityResponse {
    private LocalDateTime workdayStart;

    private LocalDateTime workdayEnd;
    private List<TimeIntervalDto> busyIntervals;
}
