package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class DColorDTO {
        private Long id;
        private String colorName;
        private String colorCode;
        private String colorMeaning;
    }

