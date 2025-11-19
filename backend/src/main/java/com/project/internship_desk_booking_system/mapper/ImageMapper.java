package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.ImageDto;
import com.project.internship_desk_booking_system.entity.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {
    public ImageDto toImageDto(Image image){
        return new ImageDto(
                image.getId(),
                image.getFileName(),
                image.getContentType(),
                image.getImageData()
        );
    }
}
