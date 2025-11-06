package com.project.internship_desk_booking_system.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterCommandRequest {
    @NotBlank(message = "First name cannot be empty")
    @Size(min = 2, max = 40, message = "First name must be between 2 and 40 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Size(min = 2, max = 40, message = "Last name must be between 2 and 40 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
    private String password;
}
