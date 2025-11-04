package com.project.internship_desk_booking_system.handlers;

import com.project.internship_desk_booking_system.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    public ResponseEntity<String> handleBadRequest(
            Exception e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(
            Exception e
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(ExceptionResponse.class)
    public ResponseEntity<Map<String, Object>> handleCustom(ExceptionResponse ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatusOverride() != null ? ex.getStatusOverride() : HttpStatus.BAD_REQUEST;

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        body.put("path", req.getRequestURI());
        body.put("details", ex.getDetails());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, Object>> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("code", "VALIDATION_FAILED");
        body.put("message", "Validation failed");
        body.put("path", req.getRequestURI());
        body.put("violations", violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<Map<String, Object>> violations = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", cv.getPropertyPath().toString(),
                        "message", cv.getMessage(),
                        "rejectedValue", cv.getInvalidValue()
                ))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("code", "VALIDATION_FAILED");
        body.put("message", "Validation failed");
        body.put("path", req.getRequestURI());
        body.put("violations", violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", "ACCESS_DENIED");
        body.put("message", "You do not have permission to access this resource");
        body.put("path", req.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", "INTERNAL_ERROR");
        body.put("message", ex.getMessage());
        body.put("path", req.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> toViolation(FieldError fe) {
        Object rejected = fe.getRejectedValue();
        if (fe.getField().toLowerCase().contains("password")) {
            rejected = "***";
        }
        return Map.of(
                "field", fe.getField(),
                "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                "rejectedValue", rejected
        );
    }

}
