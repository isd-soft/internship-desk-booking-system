package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.BookingChartDataDTO;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.dto.DeskStatsProjection;
import com.project.internship_desk_booking_system.dto.StatisticsDTO;
import com.project.internship_desk_booking_system.enums.BookingStatus;
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

/**
 * The type Statistics service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    /**
     * Gets statistics.
     *
     * @return the statistics
     */
    public StatisticsDTO getStatistics() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime weekStart = now.minusWeeks(1);
        long weeklyBookings = bookingRepository.countBookingByStartTimeAfter(weekStart);
        long weeklyUsers = userRepository.countUsersWithBookingsAfter(weekStart);
        long weeklyCancelled = bookingRepository.countByStatusAndStartTimeAfter(BookingStatus.CANCELLED, weekStart);

        LocalDateTime monthStart = now.minusMonths(1);
        long monthlyBookings = bookingRepository.countBookingByStartTimeAfter(monthStart);
        long monthlyUsers = userRepository.countUsersWithBookingsAfter(monthStart);
        long monthlyCancelled = bookingRepository.countByStatusAndStartTimeAfter(BookingStatus.CANCELLED, monthStart);

        DeskStatsDTO mostBookedDesk = convertToDTO(bookingRepository.findMostBookedDesk());
        DeskStatsDTO leastBookedDesk = convertToDTO(bookingRepository.findLeastBookedDesk());
        DeskStatsDTO mostCancelledDesk = convertToDTO(bookingRepository.findMostCancelledDesk());

        LocalDateTime dayStart = now.minusDays(1);
        List<BookingChartDataDTO> bookingHoursPerDay = convertToChartData(
                bookingRepository.countBookingsGroupedByDay(dayStart, now)
        );

        List<BookingChartDataDTO> bookingHoursPerWeek = convertToChartData(
                bookingRepository.countBookingsGroupedByWeek(weekStart, now)
        );

        List<BookingChartDataDTO> cancelledPerDay = convertToChartData(
                bookingRepository.countCancelledBookingsGroupedByDay(dayStart, now)
        );

        List<BookingChartDataDTO> cancelledPerWeek = convertToChartData(
                bookingRepository.countCancelledBookingsGroupedByWeek(weekStart, now)
        );

        double weeklyCancellationRate = calculateCancellationRate(weeklyCancelled, weeklyBookings);
        double monthlyCancellationRate = calculateCancellationRate(monthlyCancelled, monthlyBookings);

        log.info("Statistics - Weekly: {}/{} bookings ({}% cancelled), Monthly: {}/{} bookings ({}% cancelled)",
                weeklyBookings - weeklyCancelled, weeklyBookings, weeklyCancellationRate,
                monthlyBookings - monthlyCancelled, monthlyBookings, monthlyCancellationRate);

        return StatisticsDTO.builder()
                .weeklyBookings(weeklyBookings)
                .monthlyBookings(monthlyBookings)
                .weeklyUsers(weeklyUsers)
                .monthlyUsers(monthlyUsers)
                .weeklyCancelledBookings(weeklyCancelled)
                .monthlyCancelledBookings(monthlyCancelled)
                .weeklyCancellationRate(weeklyCancellationRate)
                .monthlyCancellationRate(monthlyCancellationRate)
                .mostBookedDesk(mostBookedDesk)
                .leastBookedDesk(leastBookedDesk)
                .mostCancelledDesk(mostCancelledDesk)
                .bookingHoursPerDay(bookingHoursPerDay)
                .bookingHoursPerWeek(bookingHoursPerWeek)
                .cancelledBookingsPerDay(cancelledPerDay)
                .cancelledBookingsPerWeek(cancelledPerWeek)
                .build();
    }

    /**
     * Gets statistics for date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the statistics for date range
     */
    public StatisticsDTO getStatisticsForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        long bookingsInRange = bookingRepository.countByStartTimeBetween(startDate, endDate);
        long usersInRange = userRepository.countUsersWithBookingsBetween(startDate, endDate);
        long cancelledInRange = bookingRepository.countByStatusAndStartTimeBetween(
                BookingStatus.CANCELLED, startDate, endDate
        );

        DeskStatsDTO mostBooked = convertToDTO(bookingRepository.findMostBookedDeskInRange(startDate, endDate));
        DeskStatsDTO leastBooked = convertToDTO(bookingRepository.findLeastBookedDeskInRange(startDate, endDate));
        DeskStatsDTO mostCancelledDesk = convertToDTO(bookingRepository.findMostCancelledDeskInRange(startDate, endDate));

        List<BookingChartDataDTO> bookingHoursInRange = convertToChartData(
                bookingRepository.countBookingsGroupedByDay(startDate, endDate)
        );

        List<BookingChartDataDTO> weeklyBookingsInRange = convertToChartData(
                bookingRepository.countBookingsGroupedByWeek(startDate, endDate)
        );

        List<BookingChartDataDTO> cancelledPerDay = convertToChartData(
                bookingRepository.countCancelledBookingsGroupedByDay(startDate, endDate)
        );

        List<BookingChartDataDTO> cancelledPerWeek = convertToChartData(
                bookingRepository.countCancelledBookingsGroupedByWeek(startDate, endDate)
        );

        double cancellationRate = calculateCancellationRate(cancelledInRange, bookingsInRange);

        log.info("Date Range Statistics - Bookings: {}, Cancelled: {}, Rate: {}%",
                bookingsInRange, cancelledInRange, cancellationRate);

        return StatisticsDTO.builder()
                .weeklyBookings(bookingsInRange)
                .monthlyBookings(bookingsInRange)
                .weeklyUsers(usersInRange)
                .monthlyUsers(usersInRange)
                .weeklyCancelledBookings(cancelledInRange)
                .monthlyCancelledBookings(cancelledInRange)
                .weeklyCancellationRate(cancellationRate)
                .monthlyCancellationRate(cancellationRate)
                .mostBookedDesk(mostBooked)
                .leastBookedDesk(leastBooked)
                .mostCancelledDesk(mostCancelledDesk)
                .bookingHoursPerDay(bookingHoursInRange)
                .bookingHoursPerWeek(weeklyBookingsInRange)
                .cancelledBookingsPerDay(cancelledPerDay)
                .cancelledBookingsPerWeek(cancelledPerWeek)
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

    private List<BookingChartDataDTO> convertToChartData(List<Object[]> results) {
        return results.stream()
                .map(row -> {
                    LocalDate date;
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

    private double calculateCancellationRate(long cancelled, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((cancelled * 100.0 / total) * 100.0) / 100.0;
    }
}