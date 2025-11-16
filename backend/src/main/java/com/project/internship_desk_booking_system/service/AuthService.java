package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.LoginRequestCommand;
import com.project.internship_desk_booking_system.command.LoginResponseDto;
import com.project.internship_desk_booking_system.command.RegisterCommandRequest;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.jwt.JwtUtill;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String email = authentication.getName();

            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseGet(() -> {
                        User ldapUser = User.ldapUser(email);
                        return userRepository.save(ldapUser);
                    });

            Role role = user.getRole();

            String token = jwtUtill.generateToken(email, role);

            return new LoginResponseDto(email, role, token);

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

    @Transactional
    public void register(RegisterCommandRequest request) {
        checkIfEmailExists(request.getEmail());
        validatePasswordMatch(request.getPassword(), request.getConfirmPassword());
        User newUser = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(newUser);
    }


    private void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST,
                    "PASSWORD_MISMATCH",
                    "Password and confirm password do not match");
        }
    }

    private void checkIfEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ExceptionResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                    "User with this email already exists", Map.of("email", email));
        }
    }
}
