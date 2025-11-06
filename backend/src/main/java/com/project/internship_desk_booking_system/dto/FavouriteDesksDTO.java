package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteDesksDTO {

        private Long deskId;
        private String deskName;
        private String zone;
        boolean isFavourite;
    }

