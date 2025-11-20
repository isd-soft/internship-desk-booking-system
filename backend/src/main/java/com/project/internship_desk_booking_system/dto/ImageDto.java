package com.project.internship_desk_booking_system.dto;

public record ImageDto(
        Long id,
        String file_name,
        String content_type,
        byte[] image_data,
        boolean isBackground
) { }
