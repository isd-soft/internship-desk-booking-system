//package com.project.internship_desk_booking_system;
//
//import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
//import com.project.internship_desk_booking_system.dto.DeskDto;
//import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
//import com.project.internship_desk_booking_system.entity.Booking;
//import com.project.internship_desk_booking_system.entity.Desk;
//import com.project.internship_desk_booking_system.entity.User;
//import com.project.internship_desk_booking_system.entity.Zone;
//import com.project.internship_desk_booking_system.enums.BookingStatus;
//import com.project.internship_desk_booking_system.enums.DeskStatus;
//import com.project.internship_desk_booking_system.enums.DeskType;
//import com.project.internship_desk_booking_system.command.BookingResponse;
//import com.project.internship_desk_booking_system.mapper.BookingMapper;
//import com.project.internship_desk_booking_system.mapper.DeskMapper;
//import com.project.internship_desk_booking_system.repository.BookingRepository;
//import com.project.internship_desk_booking_system.repository.DeskRepository;
//import com.project.internship_desk_booking_system.error.ExceptionResponse;
//import com.project.internship_desk_booking_system.repository.UserRepository;
//import com.project.internship_desk_booking_system.repository.ZoneRepository;
//import com.project.internship_desk_booking_system.service.AdminService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//class AdminServiceTest {
//    private static final LocalDateTime startTime = LocalDateTime.now().plusDays(1);
//    private static final LocalDateTime endTime = LocalDateTime.now().plusDays(1).plusHours(8);
//
//    @Mock
//    private DeskRepository deskRepository;
//
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ZoneRepository zoneRepository;
//
//    @Mock
//    private DeskMapper deskMapper;
//
//    @Mock
//    private BookingMapper bookingMapper;
//
//    @InjectMocks
//    private AdminService adminService;
//
//    private Desk desk;
//    private User user;
//    private Booking booking;
//    private Zone zone;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        user = new User("John", "Doe", "john@example.com", "hash");
//        user.setId(1L);
//
//        zone = new Zone();
//        zone.setId(1L);
//        zone.setZoneAbv("Ser");
//        zone.setZoneName("Service");
//
//        desk = new Desk();
//        desk.setId(5L);
//        desk.setStatus(DeskStatus.ACTIVE);
//        desk.setZone(zone);
//
//        booking = new Booking();
//        booking.setId(1L);
//        booking.setUser(user);
//        booking.setDesk(desk);
//        booking.setStartTime(startTime);
//        booking.setEndTime(endTime);
//        booking.setStatus(BookingStatus.ACTIVE);
//
//        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
//        when(zoneRepository.findById(anyLong())).thenReturn(Optional.of(zone));
//    }
//
//    @Test
//    void addDesk_success() {
//        DeskDto deskDTO = createDeskDTO();
//
//        when(deskRepository.save(any(Desk.class))).thenReturn(desk);
//        when(deskMapper.toDto(any(Desk.class))).thenReturn(deskDTO);
//
//        var result = adminService.addDesk(deskDTO);
//
//        assertEquals(DeskType.SHARED, result.type());
//        assertEquals(DeskStatus.DEACTIVATED, result.deskStatus());
//        assertFalse(result.isTemporarilyAvailable());
//
//        verify(deskRepository, times(1)).save(any(Desk.class));
//        verify(zoneRepository, times(1)).findById(1L); // теперь вызывается
//    }
//
//    @Test
//    void editDesk_success() {
//        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
//                "Desk-Updated",
//                1L, // zoneId
//                null, null, null, null, null, null, null, null, null
//        );
//
//        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
//
//        adminService.editDesk(desk.getId(), updateDTO);
//
//        verify(deskRepository, times(1)).findById(desk.getId());
//        verify(deskRepository, times(1)).save(any(Desk.class));
//        verify(zoneRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void deactivateDesk_success() {
//        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
//        when(deskMapper.toDto(any(Desk.class))).thenReturn(createDeskDTO());
//
//        var result = adminService.deactivateDesk(desk.getId());
//
//        assertEquals(DeskStatus.DEACTIVATED, result.deskStatus());
//        assertFalse(result.isTemporarilyAvailable());
//
//        verify(deskRepository, times(1)).save(any(Desk.class));
//    }
//
//    @Test
//    void deleteDesk_success() {
//        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
//        when(bookingRepository.existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class))).thenReturn(false);
//
//        adminService.deleteDesk(desk.getId());
//
//        verify(deskRepository, times(1)).deleteById(desk.getId());
//    }
//
//    @Test
//    void deleteDesk_throws_WhenHasActiveBookings() {
//        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
//        when(bookingRepository.existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class))).thenReturn(true);
//
//        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
//                () -> adminService.deleteDesk(desk.getId()));
//        assertEquals("DESK_HAS_ACTIVE_BOOKINGS", ex.getCode());
//
//        verify(deskRepository, never()).deleteById(desk.getId());
//    }
//
//    @Test
//    void editDesk_throws_WhenStartDateAfterEndDate() {
//        LocalDateTime start = LocalDateTime.now().plusDays(10);
//        LocalDateTime end = LocalDateTime.now().plusDays(5);
//
//        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
//                "Desk-Updated",
//                1L,
//                null, null, null,
//                start, end,
//                null, null, null, null
//        );
//
//        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
//
//        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
//                () -> adminService.editDesk(desk.getId(), updateDTO));
//        assertEquals("INVALID_DATE_RANGE", ex.getCode());
//    }
//
//    @Test
//    void editDesk_throws_WhenEndDateInPast() {
//        LocalDateTime start = LocalDateTime.now().minusDays(10);
//        LocalDateTime end = LocalDateTime.now().minusDays(5);
//
//        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
//                "Desk-Updated",
//                1L,
//                null, null, null,
//                start, end,
//                null, null, null, null
//        );
//
//        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
//
//        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
//                () -> adminService.editDesk(desk.getId(), updateDTO));
//        assertEquals("INVALID_DATE_RANGE", ex.getCode());
//    }
//
//    @Test
//    void editBooking_success_changeAllFields() {
//        BookingUpdateCommand command = new BookingUpdateCommand(
//                2L, 3L,
//                startTime.plusDays(1),
//                endTime.plusDays(1),
//                BookingStatus.CANCELLED
//        );
//
//        User newUser = new User("Alice", "Smith", "alice@example.com", "hash");
//        newUser.setId(2L);
//
//        Desk newDesk = new Desk();
//        newDesk.setId(3L);
//
//        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
//        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
//        when(deskRepository.findById(3L)).thenReturn(Optional.of(newDesk));
//        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());
//
//        BookingResponse response = adminService.editBooking(booking.getId(), command);
//
//        assertEquals(newUser, booking.getUser());
//        assertEquals(newDesk, booking.getDesk());
//        assertEquals(command.status(), booking.getStatus());
//    }
//
//    @Test
//    void editBooking_throws_WhenDeskNotFound() {
//        BookingUpdateCommand command = new BookingUpdateCommand(null, 999L, null, null, null);
//
//        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
//        when(deskRepository.findById(999L)).thenReturn(Optional.empty());
//
//        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
//                () -> adminService.editBooking(booking.getId(), command));
//
//        assertEquals("DESK_NOT_FOUND", ex.getCode());
//    }
//
//    @Test
//    void editBooking_throws_WhenUserNotFound() {
//        BookingUpdateCommand command = new BookingUpdateCommand(999L, null, null, null, null);
//
//        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
//        when(userRepository.findById(999L)).thenReturn(Optional.empty());
//
//        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
//                () -> adminService.editBooking(booking.getId(), command));
//
//        assertEquals("USER_NOT_FOUND", ex.getCode());
//    }
//
//    @Test
//    void editBooking_throws_WhenBookingNotFound() {
//        BookingUpdateCommand command = new BookingUpdateCommand(null, null, null, null, null);
//
//        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
//
//        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
//                () -> adminService.editBooking(999L, command));
//
//        assertEquals("BOOKING_NOT_FOUND", ex.getCode());
//    }
//
//    @Test
//    void cancelBooking_success() {
//        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
//
//        DeskDto deskDTO = new DeskDto(
//                desk.getId(), "SomeDesk", 1L, DeskType.SHARED, desk.getStatus(),
//                true, null, null, null, null, null, null);
//
//        when(deskMapper.toDto(any(Desk.class))).thenReturn(deskDTO);
//        when(bookingMapper.toResponse(any(Booking.class))).thenAnswer(invocation -> {
//            Booking b = invocation.getArgument(0);
//            return new BookingResponse(user.getId(), b.getId(), b.getStartTime(), b.getEndTime(), b.getStatus(), deskDTO);
//        });
//
//        BookingResponse response = adminService.cancelBooking(booking.getId());
//
//        assertEquals(BookingStatus.CANCELLED, response.getStatus());
//    }
//
//    private DeskDto createDeskDTO() {
//        return new DeskDto(
//                5L, "Desk-A1", 1L, DeskType.SHARED, DeskStatus.DEACTIVATED,
//                false, null, null, null, null, null, null
//        );
//    }
//}