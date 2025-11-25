package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserInfoDto {
    private String email;
    private Role role;
}
