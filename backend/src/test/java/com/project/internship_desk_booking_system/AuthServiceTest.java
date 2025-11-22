package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.LoginRequestCommand;
import com.project.internship_desk_booking_system.command.LoginResponseDto;
import com.project.internship_desk_booking_system.command.RegisterCommandRequest;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.jwt.JwtUtill;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.AuthService;
import com.project.internship_desk_booking_system.service.RsaCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtill jwtUtill;
    @Mock
    private RsaCryptoService rsaCryptoService;
    @InjectMocks
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setup() {
        // nothing
    }

    @Test
    void login_success() {
        LoginRequestCommand req = new LoginRequestCommand();
        req.setEmail("user@mail.com");
        req.setPassword("encrypted");
        when(rsaCryptoService.tryDecrypt("encrypted")).thenReturn("password");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@mail.com");
        User user = new User("user@mail.com", "hash");
        user.setRole(Role.USER);
        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(jwtUtill.generateToken("user@mail.com", Role.USER)).thenReturn("token");

        LoginResponseDto resp = authService.login(req);
        assertEquals("user@mail.com", resp.getEmail());
        assertEquals(Role.USER, resp.getRole());
        assertEquals("token", resp.getToken());
    }

    @Test
    void login_badCredentials() {
        LoginRequestCommand req = new LoginRequestCommand();
        req.setEmail("user@mail.com");
        req.setPassword("encrypted");
        when(rsaCryptoService.tryDecrypt(anyString())).thenReturn("password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> authService.login(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusOverride());
        assertEquals("AUTH_BAD_CREDENTIALS", ex.getCode());
    }

    @Test
    void login_disabled() {
        LoginRequestCommand req = new LoginRequestCommand();
        req.setEmail("user@mail.com");
        req.setPassword("encrypted");
        when(rsaCryptoService.tryDecrypt(anyString())).thenReturn("password");
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> authService.login(req));
        assertEquals("AUTH_USER_DISABLED", ex.getCode());
    }

    @Test
    void login_locked() {
        LoginRequestCommand req = new LoginRequestCommand();
        req.setEmail("user@mail.com");
        req.setPassword("encrypted");
        when(rsaCryptoService.tryDecrypt(anyString())).thenReturn("password");
        when(authenticationManager.authenticate(any())).thenThrow(new LockedException("locked"));
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> authService.login(req));
        assertEquals("AUTH_USER_LOCKED", ex.getCode());
    }

    @Test
    void login_authenticationException() {
        LoginRequestCommand req = new LoginRequestCommand();
        req.setEmail("user@mail.com");
        req.setPassword("encrypted");
        when(rsaCryptoService.tryDecrypt(anyString())).thenReturn("password");
        when(authenticationManager.authenticate(any())).thenThrow(new AuthenticationServiceException("fail"));
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> authService.login(req));
        assertEquals("AUTHENTICATION_ERROR", ex.getCode());
    }

    @Test
    void register_success() {
        RegisterCommandRequest req = new RegisterCommandRequest();
        req.setEmail("user@mail.com");
        req.setPassword("enc1");
        req.setConfirmPassword("enc2");
        when(rsaCryptoService.tryDecrypt("enc1")).thenReturn("pass");
        when(rsaCryptoService.tryDecrypt("enc2")).thenReturn("pass");
        when(userRepository.existsByEmail("user@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> authService.register(req));
    }

    @Test
    void register_emailExists() {
        RegisterCommandRequest req = new RegisterCommandRequest();
        req.setEmail("user@mail.com");
        req.setPassword("enc1");
        req.setConfirmPassword("enc2");
        when(rsaCryptoService.tryDecrypt(anyString())).thenReturn("pass");
        when(userRepository.existsByEmail("user@mail.com")).thenReturn(true);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusOverride());
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getCode());
    }

    @Test
    void register_passwordMismatch() {
        RegisterCommandRequest req = new RegisterCommandRequest();
        req.setEmail("user@mail.com");
        req.setPassword("enc1");
        req.setConfirmPassword("enc2");
        when(rsaCryptoService.tryDecrypt("enc1")).thenReturn("pass1");
        when(rsaCryptoService.tryDecrypt("enc2")).thenReturn("pass2");
        when(userRepository.existsByEmail("user@mail.com")).thenReturn(false);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> authService.register(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusOverride());
        assertEquals("PASSWORD_MISMATCH", ex.getCode());
    }
}

