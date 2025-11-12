package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private Long weeklyBookings;
    private Long monthlyBookings;
    private Long weeklyUsers;
    private Long monthlyUsers;
    private DeskStatsDTO mostBookedDesk;
    private DeskStatsDTO leastBookedDesk;
    private List<BookingResponseDto> bookingHoursPerDay;
    private List<BookingResponseDto> bookingHoursPerWeek;
}
