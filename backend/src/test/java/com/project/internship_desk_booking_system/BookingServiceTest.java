package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.BookingService;
import com.project.internship_desk_booking_system.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Desk testDesk;
    private BookingCreateRequest bookingRequest;
    private Booking testBooking;
    private BookingResponseDto bookingResponseDto;
    private BookingResponse bookingResponse;
    private DeskDto deskDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testDesk = new Desk();
        testDesk.setId(1L);
        testDesk.setZone("Service");
        testDesk.setDeskName("Ser-01");

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(4);

        bookingRequest = BookingCreateRequest.builder()
                .deskId(1L)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .desk(testDesk)
                .startTime(startTime)
                .endTime(endTime)
                .status(BookingStatus.CONFIRMED)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .deskId(1L)
                .deskName("Desk-001")
                .durationHours(5.0)
                .startTime(startTime)
                .endTime(endTime)
                .status(String.valueOf(BookingStatus.CONFIRMED))
                .build();

        DeskDto deskDTO = new DeskDto(
                1L,
                "Ser-01",
                "Service",
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                false,
                null,
                null
        );

        bookingResponse = new BookingResponse();
        bookingResponse.setBookingId(1L);
        bookingResponse.setStartTime(startTime);
        bookingResponse.setEndTime(endTime);
        bookingResponse.setStatus(BookingStatus.CONFIRMED);
        bookingResponse.setDesk(deskDTO);

    }

    @Test
    void createBooking_Success() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(deskRepository.findById(anyLong())).thenReturn(Optional.of(testDesk));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(bookingRepository.findUserBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.createBooking("test@example.com", bookingRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Desk-001", result.getDeskName());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailService, times(1)).sendBookingConfirmationEmail(
                anyString(), anyLong(), anyString(), anyString(), any());
    }


    @Test
    void getUpcomingBookingsR_Success() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eightHoursLater = now.plusHours(8);

        List<Booking> bookings = List.of(testBooking);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findUpcomingBookingsWithin8Hours(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

        List<BookingResponse> result = bookingService.getUpcomingBookingsR("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBookingId());
        assertEquals(BookingStatus.CONFIRMED, result.get(0).getStatus());
        assertNotNull(result.get(0).getDesk());
        assertEquals("Ser-01", result.get(0).getDesk().deskName());
        verify(bookingRepository, times(1)).findUpcomingBookingsWithin8Hours(
                eq(testUser.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUpcomingBookingsR_UserNotFound_ThrowsException() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.getUpcomingBookingsR("nonexistent@example.com");
        });

        assertEquals("NO_USERID_FOUND", exception.getCode());
        verify(bookingRepository, never()).findUpcomingBookingsWithin8Hours(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUpcomingBookingsR_NoBookings_ReturnsEmptyList() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findUpcomingBookingsWithin8Hours(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        List<BookingResponse> result = bookingService.getUpcomingBookingsR("test@example.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findUpcomingBookingsWithin8Hours(
                eq(testUser.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void createBooking_UserNotFound_ThrowsException() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.createBooking("nonexistent@example.com", bookingRequest);
        });

        assertEquals("NO_USERID_FOUND", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_DeskNotFound_ThrowsException() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(deskRepository.findById(anyLong())).thenReturn(Optional.empty());

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("NO_DESKID_FOUND", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_DeskNotAvailable_ThrowsException() {
        List<Booking> overlappingBookings = List.of(testBooking);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(deskRepository.findById(anyLong())).thenReturn(Optional.of(testDesk));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(overlappingBookings);

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("DESK_NOT_AVAILABLE", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_UserNotAvailable_ThrowsException() {
        List<Booking> userBookings = List.of(testBooking);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(deskRepository.findById(anyLong())).thenReturn(Optional.of(testDesk));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(bookingRepository.findUserBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(userBookings);

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("USER_NOT_AVAILABLE", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_Success() {
        LocalDateTime futureStartTime = LocalDateTime.now().plusHours(2);
        testBooking.setStartTime(futureStartTime);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        assertDoesNotThrow(() -> bookingService.cancelBooking("test@example.com", 1L));

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailService, times(1)).sendCancelledBookingEmail(
                anyString(), anyLong(), anyString(), anyString(), any());
        assertEquals(BookingStatus.CANCELLED, testBooking.getStatus());
    }

    @Test
    void cancelBooking_BookingNotFound_ThrowsException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.cancelBooking("test@example.com", 1L);
        });

        assertEquals("NO_BOOKING_FOUND", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_WrongUser_ThrowsException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.cancelBooking("different@example.com", 1L);
        });

        assertEquals("USER_CANCEL_BOOKING", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_AlreadyStarted_ThrowsException() {
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(1);
        testBooking.setStartTime(pastStartTime);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.cancelBooking("test@example.com", 1L);
        });

        assertEquals("BOOKING_ALREADY_STARTED", exception.getCode());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void validateBookingTimes_Success() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(4);

        assertDoesNotThrow(() -> bookingService.validateBookingTimes(startTime, endTime));
    }

    @Test
    void validateBookingTimes_StartTimeInPast_ThrowsException() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = startTime.plusHours(4);

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.validateBookingTimes(startTime, endTime);
        });

        assertEquals("WRONG_TIME_DATE", exception.getCode());
    }

    @Test
    void validateBookingTimes_EndTimeBeforeStartTime_ThrowsException() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(4);
        LocalDateTime endTime = startTime.minusHours(2);

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.validateBookingTimes(startTime, endTime);
        });

        assertEquals("WRONG_TIME_DATE", exception.getCode());
    }

    @Test
    void validateBookingTimes_ExceedsMaxHours_ThrowsException() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(9);

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.validateBookingTimes(startTime, endTime);
        });

        assertEquals("WRONG_TIME_DATE", exception.getCode());
    }

    @Test
    void validateBookingTimes_LessThanMinHours_ThrowsException() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(30);

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.validateBookingTimes(startTime, endTime);
        });

        assertEquals("WRONG_TIME_DATE", exception.getCode());
    }

    @Test
    void getUserBookings_Success() {
        List<Booking> bookings = List.of(testBooking);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findUserBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        List<BookingResponse> result = bookingService.getUserBookings("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findUserBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUpcomingBookings_Success() {
        List<Booking> bookings = List.of(testBooking);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findUpcomingBookingsByUserId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        List<BookingResponse> result = bookingService.getUpcomingBookingsR("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findUpcomingBookingsByUserId(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingMapper.maptoDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getBookingById("test@example.com", 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getBookingById_BookingNotFound_ThrowsException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.getBookingById("test@example.com", 1L);
        });

        assertEquals("NO_BOOKING_FOUND", exception.getCode());
    }

    @Test
    void getBookingById_WrongUser_ThrowsException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        ExceptionResponse exception = assertThrows(ExceptionResponse.class, () -> {
            bookingService.getBookingById("different@example.com", 1L);
        });

        assertEquals("BOOKING_NOT_AVAILABLE", exception.getCode());
    }

    @Test
    void deleteBooking_Success() {
        doNothing().when(bookingRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> bookingService.deleteBooking(1L));

        verify(bookingRepository, times(1)).deleteById(1L);
    }
}