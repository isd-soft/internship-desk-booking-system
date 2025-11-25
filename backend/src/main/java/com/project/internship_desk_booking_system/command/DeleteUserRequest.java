package com.project.internship_desk_booking_system.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeleteUserRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is invalid")
    private String email;
}
