package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.dto.DeskStatsProjection;
import com.project.internship_desk_booking_system.dto.StatisticsDTO;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

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
        List<BookingResponseDto> bookingHoursPerDay = bookingRepository
                .findByStartTimeBetween(dayStart, now)
                .stream()
                .map(bookingMapper::maptoDto)
                .collect(Collectors.toList());

        List<BookingResponseDto> bookingHoursPerWeek = bookingRepository
                .findByStartTimeBetween(weekStart, now)
                .stream()
                .map(bookingMapper::maptoDto)
                .collect(Collectors.toList());

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

        List<BookingResponseDto> bookingHoursInRange = bookingRepository
                .findByStartTimeBetween(startDate, endDate)
                .stream()
                .map(bookingMapper::maptoDto)
                .collect(Collectors.toList());

        log.info("Date Range Statistics - Bookings: {}, Users: {}", bookingsInRange, usersInRange);

        return StatisticsDTO.builder()
                .weeklyBookings(bookingsInRange)
                .monthlyBookings(bookingsInRange)
                .weeklyUsers(usersInRange)
                .monthlyUsers(usersInRange)
                .mostBookedDesk(mostBooked)
                .leastBookedDesk(leastBooked)
                .bookingHoursPerDay(bookingHoursInRange)
                .bookingHoursPerWeek(bookingHoursInRange)
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
}