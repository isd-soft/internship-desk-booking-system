package com.project.internship_desk_booking_system.dto;

public record ImageItemDto(
        Long id,
        String file_name,
        String content_type,
        boolean isBackground
) { }
