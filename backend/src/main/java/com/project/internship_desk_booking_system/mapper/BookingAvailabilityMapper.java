package com.project.internship_desk_booking_system.mapper;

import com.project.internship_desk_booking_system.dto.DeskAvailabilityResponse;
import com.project.internship_desk_booking_system.dto.TimeIntervalDto;
import com.project.internship_desk_booking_system.entity.Booking;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingAvailabilityMapper {

    public TimeIntervalDto toTimeIntervalDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new TimeIntervalDto(
                booking.getStartTime(),
                booking.getEndTime()
        );
    }

    public DeskAvailabilityResponse toDeskAvailabilityResponse(
            LocalDateTime workdayStart,
            LocalDateTime workdayEnd,
            List<TimeIntervalDto> busyIntervals
    ) {
        return new DeskAvailabilityResponse(
                workdayStart,
                workdayEnd,
                busyIntervals
        );
    }


}
