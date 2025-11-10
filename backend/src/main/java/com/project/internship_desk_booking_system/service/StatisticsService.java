package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.dto.StatisticsDTO;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public StatisticsDTO getStatistics() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime week = now.minusWeeks(1);
        long weeklyBookings = bookingRepository.countByStartTimeAfter(week);
        long weeklyUsers = userRepository.countUsersWithBookingsAfter(week);

        LocalDateTime month = now.minusMonths(1);
        long monthlyBookings = bookingRepository.countByStartTimeAfter(month);
        long monthlyUsers = userRepository.countUsersWithBookingsAfter(month);

        DeskStatsDTO mostBookedDesk = bookingRepository.findMostBookedDesk();
        DeskStatsDTO leastBookedDesk = bookingRepository.findLeastBookedDesk();

        return StatisticsDTO.builder()
                .weeklyBookings(weeklyBookings)
                .monthlyBookings(monthlyBookings)
                .weeklyUsers(weeklyUsers)
                .monthlyUsers(monthlyUsers)
                .mostBookedDesk(mostBookedDesk)
                .leastBookedDesk(mostBookedDesk)
                .build();
    }
    public StatisticsDTO getStatisticsForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        long bookingsInRange = bookingRepository.countByStartTimeBetween(startDate, endDate);
        long usersInRange = userRepository.countUsersWithBookingsBetween(startDate, endDate);

        DeskStatsDTO mostBooked = bookingRepository.findMostBookedDeskInRange(startDate, endDate);
        DeskStatsDTO leastBooked = bookingRepository.findLeastBookedDeskInRange(startDate, endDate);

        return StatisticsDTO.builder()
                .weeklyBookings(bookingsInRange)
                .monthlyBookings(bookingsInRange)
                .weeklyUsers(usersInRange)
                .monthlyUsers(usersInRange)
                .mostBookedDesk(mostBooked)
                .leastBookedDesk(leastBooked)
                .build();
    }
}
