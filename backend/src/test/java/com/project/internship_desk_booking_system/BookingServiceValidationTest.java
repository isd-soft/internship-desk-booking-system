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
import java.time.LocalDate;
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

    final LocalDateTime fixedNow = LocalDateTime.of(2025, 11, 22, 10, 0);

    static class TestableBookingServiceValidation extends BookingServiceValidation {
        private final LocalDateTime now;
        public TestableBookingServiceValidation(BookingRepository repo, BookingProperties props, BookingTimeLimitsService limits, LocalDateTime now) {
            super(repo, props, limits);
            this.now = now;
        }
        protected LocalDateTime now() {
            return now;
        }
    }

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
        validation = new TestableBookingServiceValidation(bookingRepository, bookingProperties, bookingTimeLimitsService, fixedNow);
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
    @Test
    void testWeeklyLimitExceeded() {
        BookingTimeLimits limits = new BookingTimeLimits();
        limits.setMaxHoursPerWeek(8);
        limits.setMaxDaysInAdvance(14);
        when(bookingTimeLimitsService.getActivePolicy()).thenReturn(limits);
        User user = new User("u@test.com", "pass");
        user.setId(2L);
        LocalDateTime monday = fixedNow.plusWeeks(1).with(DayOfWeek.MONDAY).withHour(9);
        Booking b = Booking.builder()
                .user(user)
                .desk(new Desk())
                .startTime(monday)
                .endTime(monday.plusHours(6))
                .build();
        when(bookingRepository.findUserBookings(any(), any(), any())).thenReturn(List.of(b));
        LocalDateTime start = monday.plusDays(1).withHour(10);
        LocalDateTime end = start.plusHours(3);
        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);
        req.setStartTime(start);
        req.setEndTime(end);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> validation.validateBookingLogic(user, req));
        assertEquals("WEEKLY_HOURS_EXCEEDED", ex.getCode());
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

    @Test
    void testValidateUserOtherThanAdmin_forbidden() {
        User admin = new User("admin@test.com", "pass");
        admin.setId(1L);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> validation.validateUserOtherThanAdmin(admin));
        assertEquals("USER_CANNOT_BOOK", ex.getCode());
    }

    @Test
    void testValidateTemporaryWindow_nullFromOrUntil() {
        Desk desk = new Desk();
        desk.setType(DeskType.ASSIGNED);
        desk.setIsTemporarilyAvailable(true);
        desk.setTemporaryAvailableFrom(null);
        desk.setTemporaryAvailableUntil(null);
        LocalDateTime now = LocalDateTime.now();
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                validation.validateTemporaryWindow(desk, now, now.plusHours(1)));
        assertEquals("TEMPORARY_WINDOW_INVALID", ex.getCode());
    }

    @Test
    void testValidateTemporaryWindow_outOfRange() {
        Desk desk = new Desk();
        desk.setType(DeskType.ASSIGNED);
        desk.setIsTemporarilyAvailable(true);
        LocalDateTime from = LocalDateTime.now().plusHours(1);
        LocalDateTime until = from.plusHours(2);
        desk.setTemporaryAvailableFrom(from);
        desk.setTemporaryAvailableUntil(until);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () ->
                validation.validateTemporaryWindow(desk, from.minusHours(1), until.plusHours(1)));
        assertEquals("OUT_OF_TEMPORARY_RANGE", ex.getCode());
    }

    @Test
    void testCheckDeskAvailability_alreadyBooked() {
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(true);

        User user = new User("u@test.com", "pass");
        user.setId(2L);

        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10);
        req.setStartTime(start);
        req.setEndTime(start.plusHours(2));

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> validation.validateBookingLogic(user, req));

        assertEquals("DESK_ALREADY_BOOKED", ex.getCode());
    }




    @Test
    void testValidateMaxDaysInAdvance_tooFar() {
        BookingTimeLimits limits = new BookingTimeLimits();
        limits.setMaxDaysInAdvance(1);
        limits.setMaxHoursPerWeek(40);
        when(bookingTimeLimitsService.getActivePolicy()).thenReturn(limits);

        LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(10);

        User user = new User("u@test.com", "pass");
        user.setId(2L);

        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);
        req.setStartTime(start);
        req.setEndTime(start.plusHours(1));

        lenient().when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        lenient().when(bookingRepository.existsUserConflict(any(), any(), any())).thenReturn(false);
        lenient().when(bookingRepository.findUserBookings(any(), any(), any())).thenReturn(List.of());

        ExceptionResponse ex = assertThrows(
                ExceptionResponse.class,
                () -> validation.validateBookingLogic(user, req)
        );

        assertEquals("BOOKING_TOO_FAR_AHEAD", ex.getCode());
    }



    @Test
    void testEffectiveHoursExcludingLunch() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(12);
        LocalDateTime end = start.plusHours(3);

        when(bookingProperties.getLunchStartHour()).thenReturn(13);
        when(bookingProperties.getLunchEndHour()).thenReturn(14);

        assertDoesNotThrow(() -> validation.validateBookingTimes(start, end));
    }


    @Test
    void testValidateBookingLogic_success() {
        User user = new User("u@test.com", "pass");
        user.setId(2L);
        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);
        req.setStartTime(LocalDateTime.now().plusDays(1).withHour(10));
        req.setEndTime(req.getStartTime().plusHours(2));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(bookingRepository.existsUserConflict(any(), any(), any())).thenReturn(false);
        when(bookingRepository.findUserBookings(any(), any(), any())).thenReturn(List.of());
        assertDoesNotThrow(() -> validation.validateBookingLogic(user, req));
    }

    @Test
    void testValidateBookingLogic_deskConflict() {
        User user = new User("u@test.com", "pass");
        user.setId(2L);
        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);
        req.setStartTime(LocalDateTime.now().plusDays(1).withHour(10));
        req.setEndTime(req.getStartTime().plusHours(2));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(true);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> validation.validateBookingLogic(user, req));
        // DESK_ALREADY_BOOKED
    }

    @Test
    void testValidateBookingLogic_userConflict() {
        User user = new User("u@test.com", "pass");
        user.setId(2L);
        BookingCreateRequest req = new BookingCreateRequest();
        req.setDeskId(1L);
        req.setStartTime(LocalDateTime.now().plusDays(1).withHour(10));
        req.setEndTime(req.getStartTime().plusHours(2));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(bookingRepository.existsUserConflict(any(), any(), any())).thenReturn(true);
        ExceptionResponse ex = assertThrows(ExceptionResponse.class, () -> validation.validateBookingLogic(user, req));
    }
}
