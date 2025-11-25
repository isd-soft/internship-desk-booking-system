package com.project.internship_desk_booking_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshRequest {
    private String refreshToken;
}
