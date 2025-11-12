/*
package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.dto.StatisticsDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    private DeskStatsDTO mostBookedDesk;
    private DeskStatsDTO leastBookedDesk;
    private Booking booking1;
    private Booking booking2;
    private BookingResponseDto bookingDto1;
    private BookingResponseDto bookingDto2;

    @BeforeEach
    void setUp() {
        mostBookedDesk = DeskStatsDTO.builder()
                .deskName("D-101")
                .bookingCount(50L)
                .build();

        leastBookedDesk = DeskStatsDTO.builder()
                .deskName("D-102")
                .bookingCount(5L)
                .build();

        booking1 = new Booking();
        booking2 = new Booking();

        bookingDto1 = BookingResponseDto.builder()
                .id(1L)
                .deskName("D-101")
                .build();

        bookingDto2 = BookingResponseDto.builder()
                .id(2L)
                .deskName("D-102")
                .build();
    }

    @Test
    void getStatistics_ShouldReturnCompleteStatistics() {
        // Arrange
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class)))
                .thenReturn(10L)  // weekly
                .thenReturn(25L); // monthly

        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class)))
                .thenReturn(5L)   // weekly
                .thenReturn(15L); // monthly

        when(bookingRepository.findMostBookedDesk()).thenReturn(mostBookedDesk);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(leastBookedDesk);

        List<Booking> dayBookings = Collections.singletonList(booking1);
        List<Booking> weekBookings = Arrays.asList(booking1, booking2);

        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(dayBookings)  // daily
                .thenReturn(weekBookings); // weekly

        when(bookingMapper.maptoDto(booking1)).thenReturn(bookingDto1);
        when(bookingMapper.maptoDto(booking2)).thenReturn(bookingDto2);

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeeklyBookings()).isEqualTo(10L);
        assertThat(result.getMonthlyBookings()).isEqualTo(25L);
        assertThat(result.getWeeklyUsers()).isEqualTo(5L);
        assertThat(result.getMonthlyUsers()).isEqualTo(15L);
        assertThat(result.getMostBookedDesk()).isEqualTo(mostBookedDesk);
        assertThat(result.getLeastBookedDesk()).isEqualTo(leastBookedDesk);
        assertThat(result.getBookingHoursPerDay()).hasSize(1);
        assertThat(result.getBookingHoursPerWeek()).hasSize(2);

        // Verify
        verify(bookingRepository, times(2)).countBookingByStartTimeAfter(any(LocalDateTime.class));
        verify(userRepository, times(2)).countUsersWithBookingsAfter(any(LocalDateTime.class));
        verify(bookingRepository).findMostBookedDesk();
        verify(bookingRepository).findLeastBookedDesk();
        verify(bookingRepository, times(2)).findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getStatistics_WithNoBookings_ShouldReturnZeroValues() {
        // Arrange
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class))).thenReturn(0L);
        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class))).thenReturn(0L);
        when(bookingRepository.findMostBookedDesk()).thenReturn(null);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(null);
        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeeklyBookings()).isZero();
        assertThat(result.getMonthlyBookings()).isZero();
        assertThat(result.getWeeklyUsers()).isZero();
        assertThat(result.getMonthlyUsers()).isZero();
        assertThat(result.getMostBookedDesk()).isNull();
        assertThat(result.getLeastBookedDesk()).isNull();
        assertThat(result.getBookingHoursPerDay()).isEmpty();
        assertThat(result.getBookingHoursPerWeek()).isEmpty();
    }

    @Test
    void getStatisticsForDateRange_ShouldReturnStatisticsForSpecifiedRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(15L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(8L);
        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(mostBookedDesk);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(leastBookedDesk);

        List<Booking> rangeBookings = Arrays.asList(booking1, booking2);
        when(bookingRepository.findByStartTimeBetween(startDate, endDate)).thenReturn(rangeBookings);
        when(bookingMapper.maptoDto(booking1)).thenReturn(bookingDto1);
        when(bookingMapper.maptoDto(booking2)).thenReturn(bookingDto2);

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeeklyBookings()).isEqualTo(15L);
        assertThat(result.getMonthlyBookings()).isEqualTo(15L);
        assertThat(result.getWeeklyUsers()).isEqualTo(8L);
        assertThat(result.getMonthlyUsers()).isEqualTo(8L);
        assertThat(result.getMostBookedDesk()).isEqualTo(mostBookedDesk);
        assertThat(result.getLeastBookedDesk()).isEqualTo(leastBookedDesk);
        assertThat(result.getBookingHoursPerDay()).hasSize(2);
        assertThat(result.getBookingHoursPerWeek()).hasSize(2);

        // Verify
        verify(bookingRepository).countByStartTimeBetween(startDate, endDate);
        verify(userRepository).countUsersWithBookingsBetween(startDate, endDate);
        verify(bookingRepository).findMostBookedDeskInRange(startDate, endDate);
        verify(bookingRepository).findLeastBookedDeskInRange(startDate, endDate);
        verify(bookingRepository).findByStartTimeBetween(startDate, endDate);
    }

    @Test
    void getStatisticsForDateRange_WithNoBookingsInRange_ShouldReturnZeroValues() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(0L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(0L);
        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(null);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(null);
        when(bookingRepository.findByStartTimeBetween(startDate, endDate)).thenReturn(Collections.emptyList());

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeeklyBookings()).isZero();
        assertThat(result.getMonthlyBookings()).isZero();
        assertThat(result.getWeeklyUsers()).isZero();
        assertThat(result.getMonthlyUsers()).isZero();
        assertThat(result.getMostBookedDesk()).isNull();
        assertThat(result.getLeastBookedDesk()).isNull();
        assertThat(result.getBookingHoursPerDay()).isEmpty();
        assertThat(result.getBookingHoursPerWeek()).isEmpty();
    }

    @Test
    void getStatisticsForDateRange_ShouldHandleSameDayRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 15, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 15, 23, 59);

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(3L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(2L);
        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(mostBookedDesk);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(leastBookedDesk);
        when(bookingRepository.findByStartTimeBetween(startDate, endDate))
                .thenReturn(Collections.singletonList(booking1));
        when(bookingMapper.maptoDto(booking1)).thenReturn(bookingDto1);

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeeklyBookings()).isEqualTo(3L);
        assertThat(result.getBookingHoursPerDay()).hasSize(1);
    }

    @Test
    void getStatistics_ShouldMapBookingsCorrectly() {
        // Arrange
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class))).thenReturn(1L);
        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class))).thenReturn(1L);
        when(bookingRepository.findMostBookedDesk()).thenReturn(mostBookedDesk);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(leastBookedDesk);

        List<Booking> bookings = Collections.singletonList(booking1);
        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingMapper.maptoDto(booking1)).thenReturn(bookingDto1);

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertThat(result.getBookingHoursPerDay()).contains(bookingDto1);
        assertThat(result.getBookingHoursPerWeek()).contains(bookingDto1);
        verify(bookingMapper, times(2)).maptoDto(booking1);
    }

    @Test
    void getStatisticsForDateRange_WithLargeDataset_ShouldHandleEfficiently() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(1000L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(100L);
        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(mostBookedDesk);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(leastBookedDesk);

        List<Booking> largeBookingList = Arrays.asList(booking1, booking2);
        when(bookingRepository.findByStartTimeBetween(startDate, endDate)).thenReturn(largeBookingList);
        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingDto1, bookingDto2);

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeeklyBookings()).isEqualTo(1000L);
        assertThat(result.getBookingHoursPerDay()).hasSize(2);
        verify(bookingMapper, times(2)).maptoDto(any(Booking.class));
    }
}*/
