package com.project.internship_desk_booking_system.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ExceptionResponse extends RuntimeException {

    private final String code;
    private final HttpStatus statusOverride;
    private final Map<String, Object> details;

    public ExceptionResponse(HttpStatus status, String code, String message) {
        super(message);
        this.code = code;
        this.statusOverride = status;
        this.details = null;
    }

    public ExceptionResponse(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.statusOverride = status;
        this.details = details;
    }

    public ExceptionResponse(HttpStatus status, String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.statusOverride = status;
        this.details = null;
    }
}
