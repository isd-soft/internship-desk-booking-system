package com.project.internship_desk_booking_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.internship_desk_booking_system.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Custom authentication entry point for handling unauthorized (HTTP 401) errors in Spring Security.
 * Returns a JSON error response.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    /**
     * Handles authentication exceptions and writes a JSON error response.
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the authentication exception
     * @throws IOException if writing the response fails
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        com.project.internship_desk_booking_system.error.ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "UNAUTHORIZED",
                "You must be authenticated to access this resource",
                request.getRequestURI(),
                Collections.emptyMap(),
                Collections.emptyList()
        );

        response.setStatus(status.value());
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), body);
    }
}