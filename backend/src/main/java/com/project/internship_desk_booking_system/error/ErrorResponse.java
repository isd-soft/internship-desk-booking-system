package com.project.internship_desk_booking_system.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, Object> details,
        List<FieldViolation> violations
) {
    public record FieldViolation(String field, String message, Object rejectedValue) {
    }
}