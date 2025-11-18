package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeskMapper {
    private final ZoneMapper zoneMapper;
    public DeskDto toDto(Desk desk) {
        return new DeskDto(
                desk.getId(),
                desk.getDeskName(),
                zoneMapper.toDto(desk.getZone()),
                desk.getType(),
                desk.getStatus(),
                desk.getIsTemporarilyAvailable(),
                desk.getTemporaryAvailableFrom(),
                desk.getTemporaryAvailableUntil(),
                desk.getCurrentX(),
                desk.getCurrentY(),
                desk.getBaseX(),
                desk.getBaseY(),
                desk.getHeight(),
                desk.getWidth(),
                desk.getReasonOfDeletion()
        );
    }
}
