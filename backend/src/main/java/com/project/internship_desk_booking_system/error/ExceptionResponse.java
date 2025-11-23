package com.project.internship_desk_booking_system.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Runtime exception carrying structured error information intended for API responses.
 *
 * This exception wraps an HTTP status (optional override), a machine-friendly error code,
 * a human message and arbitrary details that will be sent to clients by the global
 * exception handler.
 */
@Getter
public class ExceptionResponse extends RuntimeException {

    private final String code;
    private final HttpStatus statusOverride;
    private final Map<String, Object> details;

    /**
     * Create an ExceptionResponse with status, code and message.
     *
     * @param status HTTP status to use for response (may be used as an override)
     * @param code   machine-friendly error code (e.g. "USER_NOT_FOUND")
     * @param message human-readable message describing the error
     */
    public ExceptionResponse(HttpStatus status, String code, String message) {
        super(message);
        this.code = code;
        this.statusOverride = status;
        this.details = null;
    }

    /**
     * Create an ExceptionResponse with additional details map.
     *
     * @param status  HTTP status to use for response
     * @param code    machine-friendly error code
     * @param message human-readable message
     * @param details additional structured details to be included in the response body
     */
    public ExceptionResponse(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.statusOverride = status;
        this.details = details;
    }

    /**
     * Create an ExceptionResponse that wraps a cause.
     *
     * @param status  HTTP status
     * @param code    machine-friendly error code
     * @param message human-readable message
     * @param cause   original throwable cause
     */
    public ExceptionResponse(HttpStatus status, String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.statusOverride = status;
        this.details = null;
    }
}
