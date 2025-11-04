package com.project.internship_desk_booking_system.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestCommand {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}


