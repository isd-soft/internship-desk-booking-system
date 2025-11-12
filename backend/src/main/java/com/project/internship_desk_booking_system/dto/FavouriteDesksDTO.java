package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.entity.Zone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteDesksDTO {
    private Long deskId;
    private String deskName;
    private ZoneDto zone;
}

