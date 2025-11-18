package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TimeIntervalDto {

    private LocalDateTime start;
    private LocalDateTime end;

}
