package com.project.internship_desk_booking_system.controller;

import com.project.internship_desk_booking_system.entity.Image;
import com.project.internship_desk_booking_system.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(("/api/v1/images"))
@RestController
public class ImageController {

    private final ImageService imageService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImageById(
            @PathVariable("id") Long id
    ) {
        Image image = imageService.getImageById(id);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(
                MediaType.valueOf(image.getContentType()));

        return new ResponseEntity<>(
                image.getImageData(),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
