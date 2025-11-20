package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.config.BookingProperties;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.BookingTimeLimits;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.service.BookingServiceValidation;
import com.project.internship_desk_booking_system.service.BookingTimeLimitsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BookingServiceValidationTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    BookingProperties bookingProperties;

    @Mock
    BookingTimeLimitsService bookingTimeLimitsService;

    @InjectMocks
    BookingServiceValidation validation;

    @BeforeEach
    void setup() {
        lenient().when(bookingProperties.getOfficeStartHour()).thenReturn(9);
        lenient().when(bookingProperties.getOfficeEndHour()).thenReturn(18);
        lenient().when(bookingProperties.getLunchStartHour()).thenReturn(13);
        lenient().when(bookingProperties.getLunchEndHour()).thenReturn(14);
        lenient().when(bookingProperties.getMinHours()).thenReturn(1);
        lenient().when(bookingProperties.getMaxHours()).thenReturn(8);

        BookingTimeLimits limits = new BookingTimeLimits();
        limits.setMaxDaysInAdvance(14);
        limits.setMaxHoursPerWeek(40);

        lenient().when(bookingTimeLimitsService.getActivePolicy()).thenReturn(limits);
    }
    @Test
    void testStartInPast() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = start.plusHours(2);

        assertThrows(ExceptionResponse.class,
                () -> validation.validateBookingTimes(start, end));
    }

    @Test
    void testEndBeforeStart() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(ExceptionResponse.class,
                () -> validation.validateBookingTimes(now.plusHours(2), now.plusHours(1)));
    }

    @Test
    void testTooShort() {
        lenient().when(bookingProperties.getMinHours()).thenReturn(2);

        LocalDateTime now = LocalDateTime.now().plusHours(1);
        assertThrows(ExceptionResponse.class,
                () -> validation.validateBookingTimes(now, now.plusMinutes(30)));
    }

    @Test
    void testTooLong() {
        lenient().when(bookingProperties.getMaxHours()).thenReturn(4);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        assertThrows(ExceptionResponse.class,
                () -> validation.validateBookingTimes(start, start.plusHours(6)));
    }

    @Test
    void testOfficeHoursTooEarly() {
        LocalDateTime day = LocalDateTime.now().plusDays(1);
        assertThrows(ExceptionResponse.class,
                () -> validation.validateOfficeHours(day.withHour(7), day.withHour(10)));
    }

    @Test
    void testOfficeHoursTooLate() {
        LocalDateTime day = LocalDateTime.now().plusDays(1);
        assertThrows(ExceptionResponse.class,
                () -> validation.validateOfficeHours(day.withHour(10), day.withHour(20)));
    }

    @Test
    void testOfficeHoursOK() {
        LocalDateTime day = LocalDateTime.now().plusDays(1);
        assertDoesNotThrow(() ->
                validation.validateOfficeHours(day.withHour(10), day.withHour(17)));
    }

//    @Test
////    void testTooFarAhead() {
////        BookingTimeLimits limits = new BookingTimeLimits();
////        limits.setMaxDaysInAdvance(3);
////        limits.setMaxHoursPerWeek(40);
////
////        lenient().when(bookingTimeLimitsService.getActivePolicy()).thenReturn(limits);
////
////        User user = new User("A", "B", "test@test.com", "pass");
////
////        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(10);
////        LocalDateTime end = start.plusHours(2);
////
////        BookingCreateRequest req = new BookingCreateRequest();
////        req.setDeskId(1L);
////        req.setStartTime(start);
////        req.setEndTime(end);
////
////        lenient().when(bookingRepository.existsOverlappingBooking(any(), any(), any()))
////                .thenReturn(List.of(Booking.class));
////        lenient().when(bookingRepository.existsUserConflict(any(), any(), any()))
////                .thenReturn(false);
////        lenient().when(bookingRepository.findUserBookings(any(), any(), any()))
////                .thenReturn(List.of());
////
////        assertThrows(ExceptionResponse.class,
////                () -> validation.validateBookingLogic(user, req));
////    }
//    @Test
    void testWeeklyLimitExceeded() {
        BookingTimeLimits limits = new BookingTimeLimits();
        limits.setMaxHoursPerWeek(8);
        limits.setMaxDaysInAdvance(14);

        lenient().when(bookingTimeLimitsService.getActivePolicy()).thenReturn(limits);

        User user = new User( "u@test.com", "pass");

        LocalDateTime monday = LocalDateTime.now().with(DayOfWeek.MONDAY).withHour(9);

        Booking b = Booking.builder()
                .user(user)
                .desk(new Desk())
                .startTime(monday)
                .endTime(monday.plusHours(6))
                .build();

        lenient().when(bookingRepository.findUserBookings(any(), any(), any()))
                .thenReturn(List.of(b));

        LocalDateTime start = monday.plusDays(1).withHour(10);
        LocalDateTime end = start.plusHours(3);

        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);
        req.setStartTime(start);
        req.setEndTime(end);

        assertThrows(ExceptionResponse.class,
                () -> validation.validateBookingLogic(user, req));
    }
    @Test
    void testSharedDeskOK() {
        Desk desk = new Desk();
        desk.setType(DeskType.SHARED);

        assertDoesNotThrow(() ->
                validation.validateDeskType(desk, LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
    }

    @Test
    void testTemporaryWindowOK() {
        Desk desk = new Desk();
        desk.setType(DeskType.ASSIGNED);
        desk.setIsTemporarilyAvailable(true);

        LocalDateTime from = LocalDateTime.now().plusHours(1);
        LocalDateTime until = from.plusHours(3);

        desk.setTemporaryAvailableFrom(from);
        desk.setTemporaryAvailableUntil(until);

        assertDoesNotThrow(() ->
                validation.validateDeskType(desk, from.plusMinutes(30), from.plusHours(2)));
    }

    @Test
    void testTemporaryWindowInvalid() {
        Desk desk = new Desk();
        desk.setType(DeskType.ASSIGNED);
        desk.setIsTemporarilyAvailable(true);

        LocalDateTime from = LocalDateTime.now().plusDays(1).withHour(9);
        LocalDateTime until = from.plusHours(4);

        desk.setTemporaryAvailableFrom(from);
        desk.setTemporaryAvailableUntil(until);

        assertThrows(ExceptionResponse.class, () ->
                validation.validateDeskType(desk, until.plusHours(1), until.plusHours(2)));
    }

    @Test
    void testDeskNotBookable() {
        Desk desk = new Desk();
        desk.setType(DeskType.ASSIGNED);
        desk.setIsTemporarilyAvailable(false);

        assertThrows(ExceptionResponse.class, () ->
                validation.validateDeskType(desk, LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
    }
    @Test
    void testUserConflict() {
        lenient().when(bookingRepository.existsUserConflict(any(), any(), any()))
                .thenReturn(true);

        assertThrows(ExceptionResponse.class, () ->
                validation.checkUserAvailability(
                        1L,
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)
                ));
    }

    @Test
    void testStatusScheduled() {
        assertEquals(
                BookingStatus.SCHEDULED,
                validation.resolveStatus(LocalDateTime.now().plusHours(2))
        );
    }

    @Test
    void testStatusActive() {
        assertEquals(
                BookingStatus.ACTIVE,
                validation.resolveStatus(LocalDateTime.now().minusMinutes(1))
        );
    }
}
