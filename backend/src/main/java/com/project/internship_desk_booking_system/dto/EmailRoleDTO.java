package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.enums.Role;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class EmailRoleDTO {
    private String email;
    private Role role;

    public EmailRoleDTO(String email, Role role) {
        this.email = email;
        this.role = role;
    }

        @Override
    public String toString() {
        return "EmailRoleDTO{email='%s', role='%s'}".formatted(email, role);
    }
}
