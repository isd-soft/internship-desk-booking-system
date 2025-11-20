package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.ImageItemDto;
import com.project.internship_desk_booking_system.entity.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {
    public ImageItemDto toImageItem(Image image){
        return new ImageItemDto(
                image.getId(),
                image.getFileName(),
                image.getContentType(),
                image.isBackground()
        );
    }
}
