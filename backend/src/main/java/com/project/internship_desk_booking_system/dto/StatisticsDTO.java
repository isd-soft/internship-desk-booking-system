package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
