package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import org.springframework.stereotype.Component;

@Component
public class DeskMapper {
    public DeskDTO toDto(Desk desk) {
        return new DeskDTO(
                desk.getId(),
                desk.getDeskName(),
                desk.getZone(),
                desk.getType(),
                desk.getStatus(),
                desk.getIsTemporarilyAvailable(),
                desk.getTemporaryAvailableFrom(),
                desk.getTemporaryAvailableUntil()
        );
    }
}
