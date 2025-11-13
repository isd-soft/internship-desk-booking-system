package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.config.BookingProperties;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.ZoneDto;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.BookingService;
import com.project.internship_desk_booking_system.service.BookingTimeLimitsService;
import com.project.internship_desk_booking_system.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private BookingTimeLimitsService bookingTimeLimitsService;

    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Desk testDesk;
    private Zone testZone;
    private BookingCreateRequest bookingRequest;
    private Booking testBooking;
    private BookingResponseDto bookingResponseDto;
    private BookingResponse bookingResponse;
    private DeskDto deskDTO;
    private ZoneDto zoneDto;

    @BeforeEach
    void setUp() {
        // Setup Zone
        testZone = new Zone();
        testZone.setId(1L);
        testZone.setZoneName("Service Zone");
        testZone.setZoneAbv("SRV");

        // Setup ZoneDto
        zoneDto = new ZoneDto(1L, "Service Zone", "SRV");

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
        testDesk.setZone(testZone);

        // Setup time
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(4); // 10:00 to 14:00

        // Setup BookingCreateRequest
        bookingRequest = BookingCreateRequest.builder()
                .deskId(1L)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        // Setup Booking
        testBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .desk(testDesk)
                .startTime(startTime)
                .endTime(endTime)
                .status(BookingStatus.ACTIVE)
                .build();

        // Setup DeskDto
        deskDTO = new DeskDto(
                1L,
                "Desk-001",
                zoneDto,
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                false,
                null,
                null,
                100.0,
                200.0,
                100.0,
                200.0
        );

        // Setup BookingResponse
        bookingResponse = new BookingResponse();
        bookingResponse.setBookingId(1L);
        bookingResponse.setUserId(1L);
        bookingResponse.setStartTime(startTime);
        bookingResponse.setEndTime(endTime);
        bookingResponse.setStatus(BookingStatus.ACTIVE);
        bookingResponse.setDesk(deskDTO);

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

        // Setup BookingProperties
        lenient().when(bookingProperties.getOfficeStartHour()).thenReturn(8);
        lenient().when(bookingProperties.getOfficeEndHour()).thenReturn(18);
        lenient().when(bookingProperties.getLunchStartHour()).thenReturn(12);
        lenient().when(bookingProperties.getLunchEndHour()).thenReturn(13);
        lenient().when(bookingProperties.getMinHours()).thenReturn(1);
        lenient().when(bookingProperties.getMaxHours()).thenReturn(8);

        // Shared stubs
        lenient().when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        lenient().when(deskRepository.findById(anyLong())).thenReturn(Optional.of(testDesk));
    }

    @Test
    void createBooking_Success() {
        // Arrange
        BookingTimeLimits mockPolicy = new BookingTimeLimits();
        mockPolicy.setMaxDaysInAdvance(7);
        mockPolicy.setMaxHoursPerWeek(40);
        mockPolicy.setIsActive(true);

        when(bookingTimeLimitsService.getActivePolicy()).thenReturn(mockPolicy);

        LocalDateTime validStart = LocalDateTime.now()
                .plusDays(2)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime validEnd = validStart.plusHours(2);

        bookingRequest.setStartTime(validStart);
        bookingRequest.setEndTime(validEnd);

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(bookingRepository.findUserBookings(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act & Assert
        assertDoesNotThrow(() -> bookingService.createBooking("test@example.com", bookingRequest));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_DeskTypeAssigned_ThrowsException() {
        // Arrange
        testDesk.setType(DeskType.ASSIGNED);

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.createBooking("test@example.com", bookingRequest)
        );
        assertEquals("DESK_NOT_BOOKABLE", ex.getCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_DeskTypeUnavailable_ThrowsException() {
        // Arrange
        testDesk.setType(DeskType.UNAVAILABLE);

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.createBooking("test@example.com", bookingRequest)
        );
        assertEquals("DESK_NOT_BOOKABLE", ex.getCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_OutsideOfficeHours_StartTooEarly_ThrowsException() {
        // Arrange
        LocalDateTime earlyStart = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0);
        LocalDateTime earlyEnd = earlyStart.plusHours(4);
        bookingRequest.setStartTime(earlyStart);
        bookingRequest.setEndTime(earlyEnd);

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.createBooking("test@example.com", bookingRequest)
        );
        assertEquals("OUTSIDE_OFFICE_HOURS", ex.getCode());
    }

    @Test
    void createBooking_OutsideOfficeHours_EndTooLate_ThrowsException() {
        // Arrange
        LocalDateTime lateStart = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0);
        LocalDateTime lateEnd = lateStart.withHour(19).withMinute(0);
        bookingRequest.setStartTime(lateStart);
        bookingRequest.setEndTime(lateEnd);

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.createBooking("test@example.com", bookingRequest)
        );
        assertEquals("OUTSIDE_OFFICE_HOURS", ex.getCode());
    }

    @Test
    void validateBookingTimes_TooShort_ThrowsException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime end = start.plusMinutes(30); // Less than minimum

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.validateBookingTimes(start, end)
        );
        assertEquals("TOO_SHORT", ex.getCode());
    }

    @Test
    void validateBookingTimes_TooLong_ThrowsException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime end = start.plusHours(10); // More than maximum

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.validateBookingTimes(start, end)
        );
        assertEquals("TOO_LONG", ex.getCode());
    }

    @Test
    void createBooking_TooFarInAdvance_ThrowsException() {
        BookingTimeLimits mockPolicy = new BookingTimeLimits();
        mockPolicy.setMaxDaysInAdvance(7);
        mockPolicy.setMaxHoursPerWeek(40);
        mockPolicy.setIsActive(true);

        when(bookingTimeLimitsService.getActivePolicy()).thenReturn(mockPolicy);

        LocalDateTime farFutureStart = LocalDateTime.now().plusDays(10).withHour(9);
        LocalDateTime farFutureEnd = farFutureStart.plusHours(4);
        bookingRequest.setStartTime(farFutureStart);
        bookingRequest.setEndTime(farFutureEnd);

        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.createBooking("test@example.com", bookingRequest)
        );

        assertEquals("BOOKING_TOO_FAR_AHEAD", ex.getCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ExceedsWeeklyHoursLimit_ThrowsException() {

        BookingTimeLimits mockPolicy = new BookingTimeLimits();
        mockPolicy.setMaxDaysInAdvance(30);
        mockPolicy.setMaxHoursPerWeek(20);
        mockPolicy.setIsActive(true);

        when(bookingTimeLimitsService.getActivePolicy()).thenReturn(mockPolicy);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.plusWeeks(1)
                .with(DayOfWeek.MONDAY)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        Booking existingBooking1 = Booking.builder()
                .id(2L)
                .user(testUser)
                .desk(testDesk)
                .startTime(nextMonday.withHour(9))
                .endTime(nextMonday.withHour(17))
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking existingBooking2 = Booking.builder()
                .id(3L)
                .user(testUser)
                .desk(testDesk)
                .startTime(nextMonday.plusDays(1).withHour(9))
                .endTime(nextMonday.plusDays(1).withHour(17))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findUserBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(existingBooking1, existingBooking2));

        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDateTime newBookingStart = nextMonday.plusDays(4).withHour(10);
        LocalDateTime newBookingEnd = newBookingStart.plusHours(6);

        BookingCreateRequest newRequest = BookingCreateRequest.builder()
                .deskId(1L)
                .startTime(newBookingStart)
                .endTime(newBookingEnd)
                .build();

        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.createBooking("test@example.com", newRequest)
        );

        assertEquals("WEEKLY_HOURS_EXCEEDED", ex.getCode());
        assertTrue(ex.getMessage().contains("Cannot exceed 20 hours per week"));
        assertTrue(ex.getMessage().contains("16 hours booked"));
        assertTrue(ex.getMessage().contains("6 more hours"));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getUserBookings_Success() {
        // Arrange
        List<Booking> bookings = List.of(testBooking);
        when(bookingRepository.findBookingsByUserOrderByStartTimeDesc(any(User.class)))
                .thenReturn(bookings);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

        // Act
        List<BookingResponse> result = bookingService.getUserBookings("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBookingId());
        assertEquals(1L, result.get(0).getUserId());
        verify(bookingRepository, times(1)).findBookingsByUserOrderByStartTimeDesc(testUser);
    }

    @Test
    void getUserBookings_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.getUserBookings("nonexistent@example.com")
        );
        assertEquals("USER_NOT_FOUND", ex.getCode());
    }

    @Test
    void getUserBookings_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(bookingRepository.findBookingsByUserOrderByStartTimeDesc(any(User.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<BookingResponse> result = bookingService.getUserBookings("test@example.com");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getBookingById_BookingNotFound_ThrowsException() {
        // Arrange
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.getBookingById("test@example.com", 999L)
        );
        assertEquals("NO_BOOKING_FOUND", ex.getCode());
    }

    @Test
    void getAllBookings_Success() {
        // Arrange
        List<Booking> bookings = List.of(testBooking);
        when(bookingRepository.findAll()).thenReturn(bookings);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

        // Act
        List<BookingResponse> result = bookingService.getAllBookings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void checkUserAvailability_UserHasConflictingBooking_ThrowsException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(4);
        when(bookingRepository.findUserBookings(anyLong(), any(), any()))
                .thenReturn(List.of(testBooking));

        // Act & Assert
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                bookingService.checkUserAvailability(1L, start, end)
        );
        assertEquals("USER_NOT_AVAILABLE", ex.getCode());
    }

    @Test
    void checkUserAvailability_UserAvailable_NoException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(4);
        when(bookingRepository.findUserBookings(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        assertDoesNotThrow(() -> bookingService.checkUserAvailability(1L, start, end));
    }

    @Test
    void cancelBooking_UpdatesDeskStatus() {
        // Arrange
        LocalDateTime futureStart = LocalDateTime.now().plusHours(2);
        testBooking.setStartTime(futureStart);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);

        // Act
        bookingService.cancelBooking("test@example.com", 1L);

        // Assert
        verify(deskRepository, times(1)).save(testDesk);
        assertEquals(DeskStatus.ACTIVE, testDesk.getStatus());
    }

    @Test
    void validateBookingTimes_ValidBooking_NoException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(4);

        // Act & Assert
        assertDoesNotThrow(() -> bookingService.validateBookingTimes(start, end));
    }
}