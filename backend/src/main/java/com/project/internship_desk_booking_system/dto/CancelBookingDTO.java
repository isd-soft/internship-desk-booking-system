package com.project.internship_desk_booking_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelBookingDTO {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
