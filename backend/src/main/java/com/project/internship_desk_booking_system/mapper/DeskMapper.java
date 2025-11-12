package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeskMapper {
    public DeskDto toDto(Desk desk) {
        return new DeskDto(
                desk.getId(),
                desk.getZone().getZoneAbv()+desk.getDeskName(),
                desk.getZone().getId(),
                desk.getType(),
                desk.getStatus(),
                desk.getIsTemporarilyAvailable(),
                desk.getTemporaryAvailableFrom(),
                desk.getTemporaryAvailableUntil(),
                desk.getCurrentX(),
                desk.getCurrentY(),
                desk.getBaseX(),
                desk.getBaseY()
        );
    }
}
