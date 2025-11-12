package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
import com.project.internship_desk_booking_system.dto.DeskStatsProjection;
import com.project.internship_desk_booking_system.dto.StatisticsDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    private User testUser;
    private Desk testDesk;
    private Booking testBooking;
    private BookingResponseDto bookingResponseDto;
    private DeskStatsProjection mostBookedProjection;
    private DeskStatsProjection leastBookedProjection;

    @BeforeEach
    void setUp() {
        // Setup User
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup Desk
        testDesk = new Desk();
        testDesk.setId(1L);
        testDesk.setDeskName("Desk-001");
        testDesk.setType(DeskType.SHARED);
        testDesk.setStatus(DeskStatus.ACTIVE);

        // Setup Booking
        LocalDateTime startTime = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(4);

        testBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .desk(testDesk)
                .startTime(startTime)
                .endTime(endTime)
                .status(BookingStatus.ACTIVE)
                .build();

        // Setup BookingResponseDto
        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .deskId(1L)
                .deskName("Desk-001")
                .durationHours(4.0)
                .startTime(startTime)
                .endTime(endTime)
                .status(String.valueOf(BookingStatus.ACTIVE))
                .build();

        // Setup DeskStatsProjections
        mostBookedProjection = new DeskStatsProjection() {
            @Override
            public String getDeskName() {
                return "Desk-001";
            }

            @Override
            public Long getBookingCount() {
                return 10L;
            }
        };

        leastBookedProjection = new DeskStatsProjection() {
            @Override
            public String getDeskName() {
                return "Desk-099";
            }

            @Override
            public Long getBookingCount() {
                return 1L;
            }
        };
    }

    @Test
    void getStatistics_Success() {
        // Arrange
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class)))
                .thenReturn(50L) // weekly
                .thenReturn(200L); // monthly

        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class)))
                .thenReturn(20L) // weekly
                .thenReturn(80L); // monthly

        when(bookingRepository.findMostBookedDesk()).thenReturn(mostBookedProjection);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(leastBookedProjection);

        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testBooking));

        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.getWeeklyBookings());
        assertEquals(200L, result.getMonthlyBookings());
        assertEquals(20L, result.getWeeklyUsers());
        assertEquals(80L, result.getMonthlyUsers());

        assertNotNull(result.getMostBookedDesk());
        assertEquals("Desk-001", result.getMostBookedDesk().getDeskName());
        assertEquals(10L, result.getMostBookedDesk().getBookingCount());

        assertNotNull(result.getLeastBookedDesk());
        assertEquals("Desk-099", result.getLeastBookedDesk().getDeskName());
        assertEquals(1L, result.getLeastBookedDesk().getBookingCount());

        assertNotNull(result.getBookingHoursPerDay());
        assertEquals(1, result.getBookingHoursPerDay().size());

        assertNotNull(result.getBookingHoursPerWeek());
        assertEquals(1, result.getBookingHoursPerWeek().size());

        // Verify repository calls
        verify(bookingRepository, times(2)).countBookingByStartTimeAfter(any(LocalDateTime.class));
        verify(userRepository, times(2)).countUsersWithBookingsAfter(any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findMostBookedDesk();
        verify(bookingRepository, times(1)).findLeastBookedDesk();
        verify(bookingRepository, times(2)).findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getStatistics_NoBookings_ReturnsZeroCounts() {
        // Arrange
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class)))
                .thenReturn(0L);

        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class)))
                .thenReturn(0L);

        when(bookingRepository.findMostBookedDesk()).thenReturn(null);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(null);

        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getWeeklyBookings());
        assertEquals(0L, result.getMonthlyBookings());
        assertEquals(0L, result.getWeeklyUsers());
        assertEquals(0L, result.getMonthlyUsers());
        assertNull(result.getMostBookedDesk());
        assertNull(result.getLeastBookedDesk());
        assertTrue(result.getBookingHoursPerDay().isEmpty());
        assertTrue(result.getBookingHoursPerWeek().isEmpty());
    }

    @Test
    void getStatistics_NullDeskProjections_ReturnsNullDesks() {
        // Arrange
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class)))
                .thenReturn(10L);

        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class)))
                .thenReturn(5L);

        when(bookingRepository.findMostBookedDesk()).thenReturn(null);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(null);

        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testBooking));

        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertNotNull(result);
        assertNull(result.getMostBookedDesk());
        assertNull(result.getLeastBookedDesk());
    }

    @Test
    void getStatisticsForDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(30L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(15L);

        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(mostBookedProjection);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(leastBookedProjection);

        when(bookingRepository.findByStartTimeBetween(startDate, endDate))
                .thenReturn(List.of(testBooking));

        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(30L, result.getWeeklyBookings());
        assertEquals(30L, result.getMonthlyBookings()); // Same value for date range
        assertEquals(15L, result.getWeeklyUsers());
        assertEquals(15L, result.getMonthlyUsers()); // Same value for date range

        assertNotNull(result.getMostBookedDesk());
        assertEquals("Desk-001", result.getMostBookedDesk().getDeskName());
        assertEquals(10L, result.getMostBookedDesk().getBookingCount());

        assertNotNull(result.getLeastBookedDesk());
        assertEquals("Desk-099", result.getLeastBookedDesk().getDeskName());
        assertEquals(1L, result.getLeastBookedDesk().getBookingCount());

        assertNotNull(result.getBookingHoursPerDay());
        assertEquals(1, result.getBookingHoursPerDay().size());

        assertNotNull(result.getBookingHoursPerWeek());
        assertEquals(1, result.getBookingHoursPerWeek().size());

        // Verify repository calls
        verify(bookingRepository, times(1)).countByStartTimeBetween(startDate, endDate);
        verify(userRepository, times(1)).countUsersWithBookingsBetween(startDate, endDate);
        verify(bookingRepository, times(1)).findMostBookedDeskInRange(startDate, endDate);
        verify(bookingRepository, times(1)).findLeastBookedDeskInRange(startDate, endDate);
        verify(bookingRepository, times(1)).findByStartTimeBetween(startDate, endDate);
    }

    @Test
    void getStatisticsForDateRange_NoBookingsInRange_ReturnsZeroCounts() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now().minusDays(20);

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(0L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(0L);

        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(null);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(null);

        when(bookingRepository.findByStartTimeBetween(startDate, endDate))
                .thenReturn(new ArrayList<>());

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getWeeklyBookings());
        assertEquals(0L, result.getMonthlyBookings());
        assertEquals(0L, result.getWeeklyUsers());
        assertEquals(0L, result.getMonthlyUsers());
        assertNull(result.getMostBookedDesk());
        assertNull(result.getLeastBookedDesk());
        assertTrue(result.getBookingHoursPerDay().isEmpty());
        assertTrue(result.getBookingHoursPerWeek().isEmpty());
    }

    @Test
    void getStatisticsForDateRange_MultipleBookings_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        Booking booking1 = Booking.builder()
                .id(1L)
                .user(testUser)
                .desk(testDesk)
                .startTime(startDate.plusDays(1))
                .endTime(startDate.plusDays(1).plusHours(4))
                .status(BookingStatus.ACTIVE)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .user(testUser)
                .desk(testDesk)
                .startTime(startDate.plusDays(2))
                .endTime(startDate.plusDays(2).plusHours(4))
                .status(BookingStatus.ACTIVE)
                .build();

        when(bookingRepository.countByStartTimeBetween(startDate, endDate)).thenReturn(2L);
        when(userRepository.countUsersWithBookingsBetween(startDate, endDate)).thenReturn(1L);

        when(bookingRepository.findMostBookedDeskInRange(startDate, endDate)).thenReturn(mostBookedProjection);
        when(bookingRepository.findLeastBookedDeskInRange(startDate, endDate)).thenReturn(leastBookedProjection);

        when(bookingRepository.findByStartTimeBetween(startDate, endDate))
                .thenReturn(List.of(booking1, booking2));

        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        // Act
        StatisticsDTO result = statisticsService.getStatisticsForDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getWeeklyBookings());
        assertEquals(2, result.getBookingHoursPerDay().size());
        assertEquals(2, result.getBookingHoursPerWeek().size());
        verify(bookingMapper, times(2)).maptoDto(any(Booking.class));
    }

    @Test
    void getStatistics_WithMultipleBookings_MapsAllCorrectly() {
        // Arrange
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Booking booking = Booking.builder()
                    .id((long) i)
                    .user(testUser)
                    .desk(testDesk)
                    .startTime(LocalDateTime.now().minusDays(i))
                    .endTime(LocalDateTime.now().minusDays(i).plusHours(4))
                    .status(BookingStatus.ACTIVE)
                    .build();
            bookings.add(booking);
        }

        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class)))
                .thenReturn(100L);

        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class)))
                .thenReturn(50L);

        when(bookingRepository.findMostBookedDesk()).thenReturn(mostBookedProjection);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(leastBookedProjection);

        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getBookingHoursPerDay().size());
        assertEquals(5, result.getBookingHoursPerWeek().size());
        verify(bookingMapper, times(10)).maptoDto(any(Booking.class)); // 5 for day + 5 for week
    }

    @Test
    void convertToDTO_ValidProjection_ReturnsDTO() {
        // This is implicitly tested in other methods, but we can verify the behavior
        // through getStatistics
        when(bookingRepository.countBookingByStartTimeAfter(any(LocalDateTime.class)))
                .thenReturn(10L);

        when(userRepository.countUsersWithBookingsAfter(any(LocalDateTime.class)))
                .thenReturn(5L);

        when(bookingRepository.findMostBookedDesk()).thenReturn(mostBookedProjection);
        when(bookingRepository.findLeastBookedDesk()).thenReturn(leastBookedProjection);

        when(bookingRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        StatisticsDTO result = statisticsService.getStatistics();

        // Assert
        assertNotNull(result.getMostBookedDesk());
        assertEquals("Desk-001", result.getMostBookedDesk().getDeskName());
        assertEquals(10L, result.getMostBookedDesk().getBookingCount());

        assertNotNull(result.getLeastBookedDesk());
        assertEquals("Desk-099", result.getLeastBookedDesk().getDeskName());
        assertEquals(1L, result.getLeastBookedDesk().getBookingCount());
    }
}