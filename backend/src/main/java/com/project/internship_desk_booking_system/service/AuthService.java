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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtill;
    private final RsaCryptoService rsaCryptoService;


    public LoginResponseDto login(LoginRequestCommand request) {
        try {
            log.info("Login attempt for user: {}", request.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            rsaCryptoService.tryDecrypt(request.getPassword())
                    )
            );

            String email = authentication.getName();

            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseGet(() -> {
                        log.info("LDAP user {} not found in DB, creating new entry.", email);
                        User ldapUser = User.ldapUser(email);
                        return userRepository.save(ldapUser);
                    });

            Role role = user.getRole();

            String token = jwtUtill.generateToken(email, role);

            log.info("User {} successfully logged in.", email);
            return new LoginResponseDto(email, role, token);

        } catch (org.springframework.security.authentication.DisabledException e) {
            log.warn("Login failed: account {} is disabled.", request.getEmail());
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTH_USER_DISABLED", "User account is disabled", e);
        } catch (org.springframework.security.authentication.LockedException e) {
            log.warn("Login failed: account {} is locked.", request.getEmail());
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTH_USER_LOCKED", "User account is locked", e);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.warn("Login failed: bad credentials for {}.", request.getEmail());
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTH_BAD_CREDENTIALS", "Invalid email or password", e);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Authentication error for {}: {}", request.getEmail(), e.getMessage());
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", e.getMessage(), e);
        }
    }

    @Transactional
    public void register(RegisterCommandRequest request) {
        log.info("Registering user: {}", request.getEmail());
        String rawPassword = rsaCryptoService.tryDecrypt(request.getPassword());
        String rawConfirmPassword = rsaCryptoService.tryDecrypt(request.getConfirmPassword());

        checkIfEmailExists(request.getEmail());
        validatePasswordMatch(rawPassword, rawConfirmPassword);
        User newUser = new User(
                request.getEmail(),
                passwordEncoder.encode(rawPassword)
        );
        userRepository.save(newUser);
        log.info("User {} successfully registered.", request.getEmail());
    }


    private void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            log.warn("Password and confirm password do not match during registration.");
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST,
                    "PASSWORD_MISMATCH",
                    "Password and confirm password do not match");
        }
    }

    private void checkIfEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email {} already exists.", email);
            throw new ExceptionResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                    "User with this email already exists", Map.of("email", email));
        }
    }
}
