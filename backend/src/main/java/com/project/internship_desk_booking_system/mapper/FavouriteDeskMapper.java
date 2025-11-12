package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.FavouriteDesks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavouriteDeskMapper {
    private final ZoneMapper zoneMapper;

    public FavouriteDesksDTO toDto(FavouriteDesks favourite) {
        if (favourite == null) {
            return null;
        }
        return new FavouriteDesksDTO(
                favourite.getDesk().getId(),
                favourite.getDesk().getDeskName(),
                zoneMapper.toDto(favourite.getDesk().getZone())
        );
    }
}
