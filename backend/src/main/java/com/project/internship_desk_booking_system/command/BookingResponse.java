package com.project.internship_desk_booking_system.command;

import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long user_id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private DeskDto desk;
}
