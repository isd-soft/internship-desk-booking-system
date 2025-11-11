package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.ZoneDto;
import com.project.internship_desk_booking_system.entity.Zone;
import org.springframework.stereotype.Component;

@Component
public class ZoneMapper {

    public ZoneDto toDto(Zone zone) {
        if (zone == null) {
            return null;
        }
        return new ZoneDto(
                zone.getId(),
                zone.getZoneName(),
                zone.getZoneAbv()
        );
    }
}
