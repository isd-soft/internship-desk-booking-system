package com.project.internship_desk_booking_system.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminCreateBookingRequest {
    @Email(message = "Email format is invalid")
    private String email;
    private boolean guest;
    @NotNull(message = "Desk ID must not be null")
    private Long deskId;
    @NotNull(message = "Start time must not be null")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    @NotNull(message = "End time must not be null")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
}
