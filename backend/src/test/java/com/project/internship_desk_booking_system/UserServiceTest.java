package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.CreateBookingCommand;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.exception.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest{

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private CustomUserPrincipal principal;
    private User user;
    private Desk desk;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        principal = new CustomUserPrincipal(
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole()
        );

        desk = new Desk();
        desk.setId(1L);
    }

    @Test
    void createBooking_success() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        CreateBookingCommand command = new CreateBookingCommand(
                desk.getId(),
                start,
                end
        );

        when(userRepository.findByEmailIgnoreCase(
                principal.getEmail())).
                thenReturn(Optional.of(user)
                );

        when(deskRepository.findById(
                desk.getId()))
                .thenReturn(Optional.of(desk)
                );

        when(bookingRepository.findOverlappingBookings(
                desk.getId(),
                start,
                end))
                .thenReturn(List.of()
                );

        var bookingDTO = userService.createBooking(principal, command);

        assertEquals(desk.getId(), bookingDTO.deskId());
        assertEquals(start, bookingDTO.startDate());
        assertEquals(end, bookingDTO.endDate());
        assertEquals(BookingStatus.ACTIVE, bookingDTO.status());

        verify(
                bookingRepository,
                times(1))
                    .save(any(Booking.class)
                );
    }

    @Test
    void createBooking_throws_WhenDurationInvalid() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusMinutes(30);
        CreateBookingCommand command = new CreateBookingCommand(desk.getId(), start, end);

        when(userRepository.findByEmailIgnoreCase(
                principal.getEmail()))
                .thenReturn(Optional.of(user)
                );
        when(deskRepository.findById(
                desk.getId()))
                .thenReturn(Optional.of(desk)
                );

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> userService.createBooking(principal, command));
        assertEquals("HOURS_OUT_OF_BOUNDS", ex.getCode());
    }

    @Test
    void createBooking_throws_WhenDatesNotSame() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusDays(1);
        CreateBookingCommand command = new CreateBookingCommand(desk.getId(), start, end);

        when(userRepository.findByEmailIgnoreCase(
                principal.getEmail()))
                .thenReturn(Optional.of(user)
                );

        when(deskRepository.findById(
                desk.getId()))
                .thenReturn(Optional.of(desk)
                );

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> userService.createBooking(principal, command));
        assertEquals("HOURS_OUT_OF_BOUNDS", ex.getCode());
    }

    @Test
    void createBooking_throws_WhenDeskNotFree() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        CreateBookingCommand command = new CreateBookingCommand(desk.getId(), start, end);

        Booking existingBooking = new Booking();
        existingBooking.setStartTime(start);
        existingBooking.setEndTime(end);

        when(userRepository.findByEmailIgnoreCase(
                principal.getEmail()))
                .thenReturn(Optional.of(user)
                );

        when(deskRepository.findById(
                desk.getId()))
                .thenReturn(Optional.of(desk)
                );

        when(bookingRepository.findOverlappingBookings(
                desk.getId(), start, end))
                .thenReturn(List.of(existingBooking)
                );

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> userService.createBooking(principal, command));
        assertEquals("CROSSING_OF_BOOKING_DATES", ex.getCode());
    }

    @Test
    void deleteBooking_success() {
        Long deskId = 1L;

        when(userRepository.findByEmailIgnoreCase(
                principal.getEmail()))
                .thenReturn(Optional.of(user)
                );

        userService.deleteBooking(principal, deskId);

        verify(
                bookingRepository,
                times(1))
                    .deleteBookingByDeskId(deskId, user.getId()
                );
    }
}
