package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.Image;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * The type Image service.
 */
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;

    /**
     * Get image by id image.
     *
     * @param id the id
     * @return the image
     */
    public Image getImageById(
            Long id
    ){
        return imageRepository
                .findById(id)
                .orElseThrow(()-> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "IMAGE_NOT_FOUND",
                        String.format(
                                "Image with id %d not found",
                                id
                        )
                ));
    }

    /**
     * Get background image image.
     *
     * @return the image
     */
    public Image getBackgroundImage(){
        return imageRepository
                .findBackground()
                .orElseThrow(()-> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "IMAGE_NOT_FOUND",
                        "Background image not found"
                ));
    }

}
