package com.project.internship_desk_booking_system.dto;

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

    private Long weeklyCancelledBookings;
    private Long monthlyCancelledBookings;
    private Double weeklyCancellationRate;
    private Double monthlyCancellationRate;
    private DeskStatsDTO mostCancelledDesk;

    private List<BookingChartDataDTO> bookingHoursPerDay;
    private List<BookingChartDataDTO> bookingHoursPerWeek;
    private List<BookingChartDataDTO> cancelledBookingsPerDay;
    private List<BookingChartDataDTO> cancelledBookingsPerWeek;
}