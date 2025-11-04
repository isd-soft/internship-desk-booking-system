package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.LoginRequestCommand;
import com.project.internship_desk_booking_system.command.LoginResponseDto;
import com.project.internship_desk_booking_system.command.RegisterCommandRequest;
import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.exception.ExceptionResponse;
import com.project.internship_desk_booking_system.jwt.JwtUtill;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtill;

    public LoginResponseDto login(LoginRequestCommand request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

            String token = jwtUtill.generateToken(principal.getUsername(), principal.getRole());
            return new LoginResponseDto(principal.getEmail(), principal.getRole(), token);

        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTH_USER_DISABLED", "User account is disabled", e);
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTH_USER_LOCKED", "User account is locked", e);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTH_BAD_CREDENTIALS", "Invalid email or password", e);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", e.getMessage(), e);
        }
    }

    public void register(RegisterCommandRequest request) {
        checkIfUsernameExists(request.getEmail());
        User newUser = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRole(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(newUser);
    }

    private void checkIfUsernameExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ExceptionResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                    "User with this email already exists", Map.of("email", email));
        }
    }
}
