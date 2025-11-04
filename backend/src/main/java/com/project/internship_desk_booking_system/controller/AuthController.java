package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.command.LoginRequestCommand;
import com.project.internship_desk_booking_system.command.LoginResponseDto;
import com.project.internship_desk_booking_system.command.RegisterCommandRequest;
import com.project.internship_desk_booking_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    public final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterCommandRequest command) {
        authService.register(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestCommand command) {
        return ResponseEntity.ok().body(authService.login(command));
    }
}
