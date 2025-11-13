package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.dto.ZoneDto;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.repository.ZoneRepository;
import com.project.internship_desk_booking_system.service.AdminService;
import com.project.internship_desk_booking_system.service.BookingTimeLimitsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private static final LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime endTime = LocalDateTime.now().plusDays(1).plusHours(8);

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private DeskMapper deskMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingTimeLimitsService bookingTimeLimitsService;

    @InjectMocks
    private AdminService adminService;

    private Desk desk;
    private User user;
    private Booking booking;
    private Zone zone;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("John", "Doe", "john@example.com", "hash");
        user.setId(1L);

        zone = new Zone();
        zone.setId(1L);
        zone.setZoneAbv("SER");
        zone.setZoneName("Service");

        desk = new Desk();
        desk.setId(5L);
        desk.setStatus(DeskStatus.ACTIVE);
        desk.setZone(zone);

        booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setDesk(desk);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(BookingStatus.ACTIVE);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(zoneRepository.findById(anyLong())).thenReturn(Optional.of(zone));
    }

    @Test
    void addDesk_success() {
        DeskDto deskDTO = createDeskDTO();

        when(deskRepository.save(any(Desk.class))).thenReturn(desk);
        when(deskMapper.toDto(any(Desk.class))).thenReturn(deskDTO);

        var result = adminService.addDesk(deskDTO);

        assertEquals(DeskType.SHARED, result.type());
        assertEquals(DeskStatus.DEACTIVATED, result.deskStatus());
        assertFalse(result.isTemporarilyAvailable());

        verify(deskRepository, times(1)).save(any(Desk.class));
        verify(zoneRepository, times(1)).findById(1L);
    }

    @Test
    void editDesk_success() {
        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
                "Desk-Updated",
                1L,
                null, null, null, null, null, null, null, null, null
        );

        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));

        adminService.editDesk(desk.getId(), updateDTO);

        verify(deskRepository, times(1)).findById(desk.getId());
        verify(deskRepository, times(1)).save(any(Desk.class));
        verify(zoneRepository, times(1)).findById(1L);
    }

    @Test
    void editBooking_shouldThrowException_whenUserHasOverlappingBooking() {

        Long bookingId = 1L;
        Long userId = 2L;
        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStartTime(LocalDateTime.now().plusDays(1));
        existingBooking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        Booking overlapping = new Booking();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.findUserBookings(eq(userId), any(), any()))
                .thenReturn(List.of(overlapping));

        BookingUpdateCommand update = new BookingUpdateCommand(
                userId,
                null, null, null, null
        );

          ExceptionResponse exception = assertThrows(
                ExceptionResponse.class,
                () -> adminService.editBooking(bookingId, update)
        );

        assertEquals("USER_NOT_AVAILABLE", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusOverride());
    }

    @Test
    void editBooking_shouldThrowException_whenDeskIsNotShared() {
        Long bookingId = 1L;
        Long deskId = 10L;

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);

        Desk desk = new Desk();
        desk.setId(deskId);
        desk.setType(DeskType.UNAVAILABLE);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(deskRepository.findById(deskId)).thenReturn(Optional.of(desk));

        BookingUpdateCommand update = new BookingUpdateCommand(
                null, deskId, null, null, null
        );

        ExceptionResponse exception = assertThrows(
                ExceptionResponse.class,
                () -> adminService.editBooking(bookingId, update)
        );

        assertEquals("DESK_NOT_BOOKABLE", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusOverride());
    }

    @Test
    void deactivateDesk_success() {
        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
        when(deskMapper.toDto(any(Desk.class))).thenReturn(createDeskDTO());

        var result = adminService.deactivateDesk(desk.getId());

        assertEquals(DeskStatus.DEACTIVATED, result.deskStatus());
        assertFalse(result.isTemporarilyAvailable());

        verify(deskRepository, times(1)).save(any(Desk.class));
    }

    @Test
    void deleteDesk_success() {
        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
        when(bookingRepository.existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class))).thenReturn(false);

        adminService.deleteDesk(desk.getId());

        verify(deskRepository, times(1)).deleteById(desk.getId());
    }

    @Test
    void deleteDesk_throws_WhenHasActiveBookings() {
        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
        when(bookingRepository.existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class))).thenReturn(true);

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.deleteDesk(desk.getId()));
        assertEquals("DESK_HAS_ACTIVE_BOOKINGS", ex.getCode());

        verify(deskRepository, never()).deleteById(desk.getId());
    }

    @Test
    void editDesk_throws_WhenStartDateAfterEndDate() {
        LocalDateTime temporaryAvailableFrom = LocalDateTime.now().plusDays(10);
        LocalDateTime temporaryAvailableUntil = LocalDateTime.now().plusDays(5);

        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
                "Desk-Updated",
                1L,
                null, null, true,
                temporaryAvailableFrom, temporaryAvailableUntil,
                null, null, null, null
        );

        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editDesk(desk.getId(), updateDTO));
        assertEquals("INVALID_DATE_RANGE", ex.getCode());
    }

    @Test
    void editDesk_throws_WhenTempAvailableInPast() {
        LocalDateTime temporaryAvailableFrom = LocalDateTime.now().minusDays(10);
        LocalDateTime temporaryAvailableUntil = LocalDateTime.now().minusDays(5);

        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
                "Desk-Updated",
                1L,
                null, null, true,
                temporaryAvailableFrom, temporaryAvailableUntil,
            null, null, null, null
        );

        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editDesk(desk.getId(), updateDTO));
        assertEquals("INVALID_DATE_RANGE", ex.getCode());
    }

    @Test
    void editBooking_success_changeAllFields() {

        LocalDateTime newStart = startTime.plusDays(1);
        LocalDateTime newEnd = endTime.plusDays(1);

        BookingUpdateCommand command = new BookingUpdateCommand(
                2L,
                3L,
                newStart,
                newEnd,
                BookingStatus.CANCELLED
        );

        User newUser = new User("testFirstName", "testLastName", "test@example.com", "hashTest");
        newUser.setId(2L);

        Desk newDesk = new Desk();
        newDesk.setId(3L);
        newDesk.setType(DeskType.SHARED);

        BookingTimeLimits policy = new BookingTimeLimits();
        policy.setMaxDaysInAdvance(30);

        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setUser(user);
        booking.setDesk(desk);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(deskRepository.findById(3L)).thenReturn(Optional.of(newDesk));
        when(bookingRepository.findUserBookings(eq(2L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingTimeLimitsService.getActivePolicy()).thenReturn(policy);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        BookingResponse response = adminService.editBooking(booking.getId(), command);

        assertEquals(newUser, booking.getUser(), "User was not updated correctly");
        assertEquals(newDesk, booking.getDesk(), "Desk was not updated correctly");
        assertEquals(newStart, booking.getStartTime(), "Start time was not updated correctly");
        assertEquals(newEnd, booking.getEndTime(), "End time was not updated correctly");
        assertEquals(BookingStatus.CANCELLED, booking.getStatus(), "Status was not updated correctly");

        verify(bookingRepository).findUserBookings(eq(2L), any(), any());
        verify(bookingMapper).toResponse(any(Booking.class));
    }


    @Test
    void editBooking_throws_WhenDeskNotFound() {
        BookingUpdateCommand command = new BookingUpdateCommand(null, 999L, null, null, null);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(deskRepository.findById(999L)).thenReturn(Optional.empty());

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editBooking(booking.getId(), command));

        assertEquals("DESK_NOT_FOUND", ex.getCode());
    }

    @Test
    void editBooking_throws_WhenUserNotFound() {
        BookingUpdateCommand command = new BookingUpdateCommand(999L, null, null, null, null);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editBooking(booking.getId(), command));

        assertEquals("USER_NOT_FOUND", ex.getCode());
    }

    @Test
    void editBooking_throws_WhenBookingNotFound() {
        BookingUpdateCommand command = new BookingUpdateCommand(null, null, null, null, null);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editBooking(999L, command));

        assertEquals("BOOKING_NOT_FOUND", ex.getCode());
    }

    @Test
    void cancelBooking_success() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        DeskDto deskDTO = createDeskDTO();
        when(deskMapper.toDto(any(Desk.class))).thenReturn(deskDTO);
        when(bookingMapper.toResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return new BookingResponse(user.getId(), b.getId(), b.getStartTime(), b.getEndTime(), b.getStatus(), deskDTO);
        });

        BookingResponse response = adminService.cancelBooking(booking.getId());
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
    }

    // ---------- Helper DTO Builders ----------

    private ZoneDto createZoneDTO() {
        return new ZoneDto(1L, "Service", "SER");
    }

    private DeskDto createDeskDTO() {
        return new DeskDto(
                5L,
                "Desk-A1",
                createZoneDTO(),
                DeskType.SHARED,
                DeskStatus.DEACTIVATED,
                false,
                null, null, null, null, null, null
        );
    }
}
