package com.project.internship_desk_booking_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.internship_desk_booking_system.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        HttpStatus status = HttpStatus.FORBIDDEN;

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                request.getRequestURI(),
                Collections.emptyMap(),
                Collections.emptyList()
        );

        response.setStatus(status.value());
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), body);
    }
}