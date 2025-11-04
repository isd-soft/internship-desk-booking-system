package com.project.internship_desk_booking_system.command;

import com.project.internship_desk_booking_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String email;
    private Role role;
    private String token;}
