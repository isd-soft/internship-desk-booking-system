package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.entity.Zone;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.BookingService;
import com.project.internship_desk_booking_system.service.BookingServiceValidation;
import com.project.internship_desk_booking_system.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    private BookingServiceValidation validation;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Desk desk;

    @BeforeEach
    void setup() {
        user = new User("test@example.com", "pass");
        user.setId(1L);

        desk = new Desk();
        desk.setId(10L);
        desk.setDeskName("Desk 10");

        Zone zone = new Zone();
        zone.setZoneAbv("A1");
        desk.setZone(zone);

        desk.setStatus(DeskStatus.ACTIVE);

        lenient().when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        lenient().when(deskRepository.findById(10L)).thenReturn(Optional.of(desk));
    }

    @Test
    void testCreateBooking_success() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(10L);
        req.setStartTime(LocalDateTime.now().plusHours(2));
        req.setEndTime(req.getStartTime().plusHours(3));

        when(validation.resolveStatus(any())).thenReturn(BookingStatus.SCHEDULED);
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0, Booking.class);
            b.setId(100L);
            return b;
        });

        assertDoesNotThrow(() -> bookingService.createBooking("test@example.com", req));

        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();

        assertEquals(100L, saved.getId());

        // Now matching works
        verify(emailService).sendBookingConfirmationEmail(
                eq("test@example.com"),
                eq(100L),
                eq("Desk 10"),
                eq("A1"),
                any()
        );
    }

    @Test
    void testCreateBooking_userNotFound() {
        when(userRepository.findByEmailIgnoreCase("unknown@mail.com"))
                .thenReturn(Optional.empty());

        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(10L);

        assertThrows(ExceptionResponse.class,
                () -> bookingService.createBooking("unknown@mail.com", req));
    }

    @Test
    void testCreateBooking_deskNotFound() {
        when(deskRepository.findById(99L)).thenReturn(Optional.empty());

        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(99L);

        assertThrows(ExceptionResponse.class,
                () -> bookingService.createBooking("test@example.com", req));
    }

    @Test
    void testCancelBooking_success() {
        Booking b = Booking.builder()
                .id(200L)
                .user(user)
                .desk(desk)
                .status(BookingStatus.ACTIVE)
                .build();

        when(bookingRepository.findByUserEmailAndId("test@example.com", 200L))
                .thenReturn(Optional.of(b));

        assertDoesNotThrow(() -> bookingService.cancelBooking("test@example.com", 200L));

        assertEquals(BookingStatus.CANCELLED, b.getStatus());

        verify(emailService).sendCancelledBookingEmail(
                eq("test@example.com"),
                eq(200L),
                eq("Desk 10"),
                eq("A1"),
                any()
        );
    }

    @Test
    void testCancelBooking_notFound() {
        when(bookingRepository.findByUserEmailAndId(any(), any())).thenReturn(Optional.empty());

        assertThrows(ExceptionResponse.class,
                () -> bookingService.cancelBooking("test@example.com", 99L));
    }

    @Test
    void testCancelBooking_wrongStatus() {
        Booking b = Booking.builder()
                .id(200L)
                .user(user)
                .desk(desk)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findByUserEmailAndId(any(), any()))
                .thenReturn(Optional.of(b));

        assertThrows(ExceptionResponse.class,
                () -> bookingService.cancelBooking("test@example.com", 200L));
    }

    @Test
    void testGetUpcomingBookings() {
        Booking a = Booking.builder().status(BookingStatus.ACTIVE).build();
        Booking s = Booking.builder().status(BookingStatus.SCHEDULED).build();

        when(bookingRepository.findByUserEmailAndStatusOrderByStartTimeAsc("test@example.com", BookingStatus.ACTIVE))
                .thenReturn(List.of(a));
        when(bookingRepository.findByUserEmailAndStatusOrderByStartTimeAsc("test@example.com", BookingStatus.SCHEDULED))
                .thenReturn(List.of(s));

        when(bookingMapper.toResponse(a)).thenReturn(new BookingResponse());
        when(bookingMapper.toResponse(s)).thenReturn(new BookingResponse());

        List<BookingResponse> result =
                bookingService.getUpcomingBookings("test@example.com");

        assertEquals(2, result.size());
    }

    @Test
    void testGetUserBookings_success() {
        Booking b = new Booking();
        when(bookingRepository.findBookingsByUserOrderByStartTimeDesc(user))
                .thenReturn(List.of(b));
        when(bookingMapper.toResponse(b)).thenReturn(new BookingResponse());

        List<BookingResponse> result =
                bookingService.getUserBookings("test@example.com");

        assertEquals(1, result.size());
    }

    @Test
    void testGetUserBookings_userNotFound() {
        when(userRepository.findByEmailIgnoreCase("wrong@mail")).thenReturn(Optional.empty());

        assertThrows(ExceptionResponse.class,
                () -> bookingService.getUserBookings("wrong@mail"));
    }

    @Test
    void testGetBookingById_success() {
        Booking b = new Booking();
        b.setId(123L);
        b.setUser(user);

        when(bookingRepository.findById(123L)).thenReturn(Optional.of(b));
        when(bookingMapper.maptoDto(b)).thenReturn(new BookingResponseDto());

        BookingResponseDto dto =
                bookingService.getBookingById("test@example.com", 123L);

        assertNotNull(dto);
    }

    @Test
    void testGetBookingById_wrongUser() {
        User another = new User("not@user.com", "pass");

        Booking b = new Booking();
        b.setUser(another);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(ExceptionResponse.class,
                () -> bookingService.getBookingById("test@example.com", 1L));
    }

    @Test
    void testGetAllBookings() {
        Booking b = new Booking();
        when(bookingRepository.findAll()).thenReturn(List.of(b));
        when(bookingMapper.toResponse(b)).thenReturn(new BookingResponse());

        var list = bookingService.getAllBookings();
        assertEquals(1, list.size());
    }

    @Test
    void testGetAllUserBookingsByDate_success() {
        LocalDate date = LocalDate.now();

        Booking b = new Booking();
        when(bookingRepository.findUserBookingsByDateNotCancelled(1L, date))
                .thenReturn(List.of(b));
        when(bookingMapper.toResponse(b)).thenReturn(new BookingResponse());

        var result = bookingService.getAllUserBookingsByDate("test@example.com", date);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllUserBookingsByDate_notFound() {
        when(bookingRepository.findUserBookingsByDateNotCancelled(any(), any()))
                .thenReturn(List.of());

        assertThrows(ExceptionResponse.class,
                () -> bookingService.getAllUserBookingsByDate("test@example.com", LocalDate.now()));
    }
}
