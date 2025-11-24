package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.BookingChartDataDTO;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.dto.DeskStatsProjection;
import com.project.internship_desk_booking_system.dto.StatisticsDTO;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public StatisticsDTO getStatistics() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime weekStart = now.minusWeeks(1);
        long weeklyBookings = bookingRepository.countBookingByStartTimeAfter(weekStart);
        long weeklyUsers = userRepository.countUsersWithBookingsAfter(weekStart);

        LocalDateTime monthStart = now.minusMonths(1);
        long monthlyBookings = bookingRepository.countBookingByStartTimeAfter(monthStart);
        long monthlyUsers = userRepository.countUsersWithBookingsAfter(monthStart);

        DeskStatsDTO mostBookedDesk = convertToDTO(bookingRepository.findMostBookedDesk());
        DeskStatsDTO leastBookedDesk = convertToDTO(bookingRepository.findLeastBookedDesk());

        LocalDateTime dayStart = now.minusDays(1);
        List<BookingChartDataDTO> bookingHoursPerDay = convertToChartData(
                bookingRepository.countBookingsGroupedByDay(dayStart, now)
        );

        List<BookingChartDataDTO> bookingHoursPerWeek = convertToChartData(
                bookingRepository.countBookingsGroupedByWeek(weekStart, now)
        );

        log.info("Statistics - Weekly Bookings: {}, Monthly Bookings: {}", weeklyBookings, monthlyBookings);
        log.info("Most Booked Desk: {}, Least Booked Desk: {}", mostBookedDesk, leastBookedDesk);

        return StatisticsDTO.builder()
                .weeklyBookings(weeklyBookings)
                .monthlyBookings(monthlyBookings)
                .weeklyUsers(weeklyUsers)
                .monthlyUsers(monthlyUsers)
                .mostBookedDesk(mostBookedDesk)
                .leastBookedDesk(leastBookedDesk)
                .bookingHoursPerDay(bookingHoursPerDay)
                .bookingHoursPerWeek(bookingHoursPerWeek)
                .build();
    }

    public StatisticsDTO getStatisticsForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        long bookingsInRange = bookingRepository.countByStartTimeBetween(startDate, endDate);
        long usersInRange = userRepository.countUsersWithBookingsBetween(startDate, endDate);

        DeskStatsDTO mostBooked = convertToDTO(bookingRepository.findMostBookedDeskInRange(startDate, endDate));
        DeskStatsDTO leastBooked = convertToDTO(bookingRepository.findLeastBookedDeskInRange(startDate, endDate));

        List<BookingChartDataDTO> bookingHoursInRange = convertToChartData(
                bookingRepository.countBookingsGroupedByDay(startDate, endDate)
        );

        List<BookingChartDataDTO> weeklyBookingsInRange = convertToChartData(
                bookingRepository.countBookingsGroupedByWeek(startDate, endDate)
        );

        log.info("Date Range Statistics - Bookings: {}, Users: {}", bookingsInRange, usersInRange);

        return StatisticsDTO.builder()
                .weeklyBookings(bookingsInRange)
                .monthlyBookings(bookingsInRange)
                .weeklyUsers(usersInRange)
                .monthlyUsers(usersInRange)
                .mostBookedDesk(mostBooked)
                .leastBookedDesk(leastBooked)
                .bookingHoursPerDay(bookingHoursInRange)
                .bookingHoursPerWeek(weeklyBookingsInRange)
                .build();
    }

    private DeskStatsDTO convertToDTO(DeskStatsProjection projection) {
        if (projection == null) {
            log.warn("Desk stats projection is null");
            return null;
        }
        return DeskStatsDTO.builder()
                .deskName(projection.getDeskName())
                .bookingCount(projection.getBookingCount())
                .build();
    }

    /**
     * Converts raw query results (Object[]) to BookingChartDataDTO list
     * Expected format: [Date, Long] from SQL query
     */
    private List<BookingChartDataDTO> convertToChartData(List<Object[]> results) {
        return results.stream()
                .map(row -> {
                    LocalDate date;
                    // Handle both java.sql.Date and java.time.LocalDate
                    if (row[0] instanceof Date) {
                        date = ((Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof LocalDate) {
                        date = (LocalDate) row[0];
                    } else {
                        date = LocalDate.parse(row[0].toString());
                    }

                    Long count = row[1] instanceof Number
                            ? ((Number) row[1]).longValue()
                            : Long.parseLong(row[1].toString());

                    return BookingChartDataDTO.builder()
                            .date(date)
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());
    }
}