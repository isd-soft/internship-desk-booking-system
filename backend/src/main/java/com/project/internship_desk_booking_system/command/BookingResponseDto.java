package com.project.internship_desk_booking_system.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.internship_desk_booking_system.enums.DeskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class BookingResponseDto {
    private Long id;
    private Long userId;
    private Long deskId;
    private DeskType deskType;
    private String deskName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    private String status;
    private Double durationHours;
}
