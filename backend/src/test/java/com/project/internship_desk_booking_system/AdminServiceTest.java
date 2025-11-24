package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.*;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.mapper.ImageMapper;
import com.project.internship_desk_booking_system.mapper.ZoneMapper;
import com.project.internship_desk_booking_system.repository.*;
import com.project.internship_desk_booking_system.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeskMapper deskMapper;

    @Mock
    private BookingTimeLimitsService bookingTimeLimitsService;

    @Mock
    private BookingServiceValidation bookingValidation;

    @Mock
    private ZoneMapper zoneMapper;

    @Mock
    private AdminServiceValidation adminServiceValidation;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private FavouriteDesksService favouriteDesksService;

    @Mock
    private DeskDiffService diffService;

    @InjectMocks
    private AdminService adminService;

    private Zone testZone;
    private Desk testDesk;
    private User testUser;
    private Booking testBooking;
    private DeskDto testDeskDto;
    private ZoneDto testZoneDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminService, "defaultAdminId", 1L);

        testZone = new Zone();
        testZone.setId(1L);
        testZone.setZoneName("Test Zone");
        testZone.setZoneAbv("TZ");

        testDesk = new Desk();
        testDesk.setId(1L);
        testDesk.setDeskName("Desk 1");
        testDesk.setZone(testZone);
        testDesk.setType(DeskType.SHARED);
        testDesk.setStatus(DeskStatus.ACTIVE);
        testDesk.setCurrentX(10.0);
        testDesk.setCurrentY(20.0);
        testDesk.setBaseX(10.0);
        testDesk.setBaseY(20.0);
        testDesk.setHeight(100L);
        testDesk.setWidth(100L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setDesk(testDesk);
        testBooking.setStartTime(LocalDateTime.now().plusDays(1));
        testBooking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        testBooking.setStatus(BookingStatus.CONFIRMED);

        testZoneDto = new ZoneDto(1L, "Test Zone", "TZ");
        testDeskDto = new DeskDto(
                1L,
                "Desk 1",
                testZoneDto,
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                false,
                null,
                null,
                10.0,
                20.0,
                10.0,
                20.0,
                100L,
                100L,
                null
        );
    }

    @Test
    @DisplayName("Should successfully delete non-default admin user")
    void deleteUser_ShouldDeleteUser_WhenNotDefaultAdmin() {
        Long userId = 2L;

        adminService.deleteUser(userId);

        verify(deskRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should return desk DTO when desk exists")
    void getDeskById_ShouldReturnDeskDto_WhenDeskExists() {
        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(deskMapper.toDto(testDesk)).thenReturn(testDeskDto);

        DeskDto result = adminService.getDeskById(1L);

        assertThat(result).isNotNull();
        assertThat(result.displayName()).isEqualTo("Desk 1");
        verify(deskRepository).findById(1L);
        verify(deskMapper).toDto(testDesk);
    }

    @Test
    @DisplayName("Should throw exception when desk not found")
    void getDeskById_ShouldThrowException_WhenDeskNotFound() {
        when(deskRepository.findById(1L)).thenReturn(Optional.empty());

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.getDeskById(1L),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Desk with id 1 not found");
    }

    @Test
    @DisplayName("Should successfully add new desk")
    void addDesk_ShouldCreateDesk_WithProvidedDetails() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(testZone));
        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);
        when(deskMapper.toDto(any(Desk.class))).thenReturn(testDeskDto);

        DeskDto result = adminService.addDesk(testDeskDto);

        assertThat(result).isNotNull();
        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());

        Desk savedDesk = deskCaptor.getValue();
        assertThat(savedDesk.getDeskName()).isEqualTo("Desk 1");
        assertThat(savedDesk.getType()).isEqualTo(DeskType.SHARED);
        assertThat(savedDesk.getStatus()).isEqualTo(DeskStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when zone not found during desk creation")
    void addDesk_ShouldThrowException_WhenZoneNotFound() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.addDesk(testDeskDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Zone not found");
    }

    @Test
    @DisplayName("Should deactivate desk when no active bookings")
    void deactivateDesk_ShouldDeactivate_WhenNoActiveBookings() {
        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(bookingRepository.existsActiveBookingsByDeskId(eq(1L), any()))
                .thenReturn(false);

        when(bookingRepository.existsScheduledBookingsByDeskId(eq(1L), any()))
                .thenReturn(false);

        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);
        when(deskMapper.toDto(any(Desk.class))).thenReturn(testDeskDto);

        DeskDto result = adminService.deactivateDesk(1L);

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());

        Desk deactivatedDesk = deskCaptor.getValue();
        assertThat(deactivatedDesk.getStatus()).isEqualTo(DeskStatus.DEACTIVATED);
        assertThat(deactivatedDesk.getIsTemporarilyAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when deactivating desk with active bookings")
    void deactivateDesk_ShouldThrowException_WhenActiveBookingsExist() {
        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(bookingRepository.existsActiveBookingsByDeskId(eq(1L), any())).thenReturn(true);

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.deactivateDesk(1L),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).isEqualTo("Can't deactivate desk with active booking on it");
        verify(deskRepository, never()).save(any(Desk.class));
    }

    @Test
    @DisplayName("Should cancel scheduled bookings and send emails when deactivating desk")
    void deactivateDesk_ShouldCancelScheduledBookings_AndSendEmails() {
        List<Booking> scheduledBookings = Arrays.asList(testBooking);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));

        when(bookingRepository.existsActiveBookingsByDeskId(eq(1L), any()))
                .thenReturn(false);

        when(bookingRepository.existsScheduledBookingsByDeskId(eq(1L), any()))
                .thenReturn(true);

        when(bookingRepository.findScheduledBookingsByDeskId(eq(1L)))
                .thenReturn(scheduledBookings);

        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);
        when(deskMapper.toDto(any(Desk.class))).thenReturn(testDeskDto);

        adminService.deactivateDesk(1L);

        verify(bookingRepository).saveAll(scheduledBookings);
        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        verify(emailService).sendCancelledBookingEmail(
                eq("test@example.com"),
                eq(1L),
                eq("Desk 1"),
                eq("TZ"),
                any(OffsetDateTime.class)
        );
    }


    @Test
    @DisplayName("Should activate desk successfully")
    void activateDesk_ShouldActivateDesk() {
        testDesk.setStatus(DeskStatus.DEACTIVATED);
        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);
        when(deskMapper.toDto(any(Desk.class))).thenReturn(testDeskDto);

        DeskDto result = adminService.activateDesk(1L);

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());

        assertThat(deskCaptor.getValue().getStatus()).isEqualTo(DeskStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should edit desk display name")
    void editDesk_ShouldUpdateDisplayName() {
        DeskUpdateDTO updates = new DeskUpdateDTO(
                "New Desk Name",
                null, null, null, null, null, null, null,
                null, null, null, null, null
        );

        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(diffService.copy(any(Desk.class))).thenReturn(testDesk);
        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);
        when(diffService.diff(any(Desk.class), any(Desk.class))).thenReturn(Arrays.asList("Name changed"));
        when(deskMapper.toDto(any(Desk.class))).thenReturn(testDeskDto);

        adminService.editDesk(1L, updates);

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());
        assertThat(deskCaptor.getValue().getDeskName()).isEqualTo("New Desk Name");
        verify(favouriteDesksService).notifyUsersAboutDeskChanges(any(Desk.class), anyList());
    }

    @Test
    @DisplayName("Should edit desk zone")
    void editDesk_ShouldUpdateZone() {
        Zone newZone = new Zone();
        newZone.setId(2L);
        newZone.setZoneName("New Zone");

        DeskUpdateDTO updates = new DeskUpdateDTO(
                null, 2L, null, null, null, null, null, null,
                null, null, null, null, null
        );

        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(zoneRepository.findById(2L)).thenReturn(Optional.of(newZone));
        when(diffService.copy(any(Desk.class))).thenReturn(testDesk);
        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);
        when(diffService.diff(any(Desk.class), any(Desk.class))).thenReturn(Arrays.asList("Zone changed"));
        when(deskMapper.toDto(any(Desk.class))).thenReturn(testDeskDto);

        adminService.editDesk(1L, updates);

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());
        assertThat(deskCaptor.getValue().getZone().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should soft delete desk when no active bookings")
    void deleteDesk_ShouldSoftDelete_WhenNoActiveBookings() {
        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(bookingRepository.existsActiveBookingsByDeskId(eq(1L), any()))
                .thenReturn(false);

        when(bookingRepository.existsScheduledBookingsByDeskId(eq(1L), any()))
                .thenReturn(false);

        adminService.deleteDesk(1L, "Maintenance required");

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());

        Desk deletedDesk = deskCaptor.getValue();
        assertThat(deletedDesk.getIsDeleted()).isTrue();
        assertThat(deletedDesk.getDeletedAt()).isNotNull();
        assertThat(deletedDesk.getReasonOfDeletion()).isEqualTo("Maintenance required");
    }

    @Test
    @DisplayName("Should throw exception when deleting desk with active bookings")
    void deleteDesk_ShouldThrowException_WhenActiveBookingsExist() {
        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(bookingRepository.existsActiveBookingsByDeskId(eq(1L), any())).thenReturn(true);

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.deleteDesk(1L, "Test reason"),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).isEqualTo("Can't delete desk with active booking on it");
    }

    @Test
    @DisplayName("Should restore deleted desk")
    void restoreDesk_ShouldRestoreDesk() {
        testDesk.setIsDeleted(true);
        testDesk.setDeletedAt(LocalDateTime.now());

        when(deskRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testDesk));

        adminService.restoreDesk(1L);

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());

        Desk restoredDesk = deskCaptor.getValue();
        assertThat(restoredDesk.getIsDeleted()).isFalse();
        assertThat(restoredDesk.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("Should cancel booking and send email")
    void cancelBooking_ShouldCancelAndSendEmail() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(mock(BookingResponse.class));

        adminService.cancelBooking(1L, "Admin cancelled");

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(emailService).sendCancelBookingAdminEmail(
                eq("Admin cancelled"),
                eq("test@example.com"),
                eq(1L),
                eq("Desk 1"),
                eq("Test Zone"),
                any(OffsetDateTime.class)
        );
    }

    @Test
    @DisplayName("Should throw exception when booking already cancelled")
    void cancelBooking_ShouldThrowException_WhenAlreadyCancelled() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.cancelBooking(1L, "Test"),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("This booking is already cancelled");
    }

    @Test
    @DisplayName("Should edit booking user")
    void editBooking_ShouldChangeUser() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("newuser@example.com");

        BookingUpdateCommand command = new BookingUpdateCommand(
                2L, null, null, null, null
        );

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        BookingTimeLimits limits = new BookingTimeLimits();
        limits.setMaxDaysInAdvance(30);

        adminService.editBooking(1L, command);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when new user has conflicting booking")
    void editBooking_ShouldThrowException_WhenUserNotAvailable() {
        Booking conflictingBooking = new Booking();
        conflictingBooking.setId(2L);

        BookingUpdateCommand command = new BookingUpdateCommand(
                2L, null, testBooking.getStartTime(), testBooking.getEndTime(), null
        );

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.findUserBookings(eq(2L), any(), any()))
                .thenReturn(Arrays.asList(conflictingBooking));

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.editBooking(1L, command),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("User already has a booking in this time period");
    }

    @Test
    @DisplayName("Should update desk coordinates")
    void changeCurrentCoordinates_ShouldUpdateCoordinates() {
        CoordinatesUpdateCommand command = new CoordinatesUpdateCommand(1L, 50.0, 60.0);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(testDesk));
        when(deskRepository.save(any(Desk.class))).thenReturn(testDesk);

        DeskCoordinatesDTO result = adminService.changeCurrentCoordinates(command);

        ArgumentCaptor<Desk> deskCaptor = ArgumentCaptor.forClass(Desk.class);
        verify(deskRepository).save(deskCaptor.capture());

        Desk updatedDesk = deskCaptor.getValue();
        assertThat(updatedDesk.getCurrentX()).isEqualTo(50.0);
        assertThat(updatedDesk.getCurrentY()).isEqualTo(60.0);
    }

    @Test
    @DisplayName("Should return all zones")
    void getAllZones_ShouldReturnAllZones() {
        List<Zone> zones = Arrays.asList(testZone);
        when(zoneRepository.findAll()).thenReturn(zones);
        when(zoneMapper.toDto(any(Zone.class))).thenReturn(testZoneDto);

        List<ZoneDto> result = adminService.getAllZones();

        assertThat(result).hasSize(1);
        verify(zoneRepository).findAll();
    }

    @Test
    @DisplayName("Should update user role successfully")
    void updateUserRole_ShouldUpdateRole() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("user@example.com");
        targetUser.setRole(Role.USER);

        CustomUserPrincipal principal = new CustomUserPrincipal("admin@example.com",
                "$2a$12$vqy.rV4j2DlPt97g/uqjweo9ZoWY7QP7Od2jK.l/cYMXGyD8TZP9e",Role.ADMIN);
        EmailRoleDTO dto = new EmailRoleDTO("user@example.com", Role.ADMIN);

        when(userRepository.findByEmailIgnoreCase("admin@example.com"))
                .thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        EmailRoleDTO result = adminService.updateUserRole(dto, principal);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should upload image successfully")
    void uploadImage_ShouldSaveImage() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

        adminService.uploadImage(file);

        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
        verify(imageRepository).save(imageCaptor.capture());

        Image savedImage = imageCaptor.getValue();
        assertThat(savedImage.getFileName()).isEqualTo("test.jpg");
        assertThat(savedImage.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("Should throw exception when image read fails")
    void uploadImage_ShouldThrowException_WhenReadFails() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenThrow(new RuntimeException("Read error"));

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.uploadImage(file),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getMessage()).isEqualTo("Can't read image file");
    }

    @Test
    @DisplayName("Should apply temporary availability with valid dates")
    void applyTemporaryAvailability_ShouldSetDates_WhenValid() {
        Desk desk = new Desk();
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime until = LocalDateTime.now().plusDays(5);

        adminService.applyTemporaryAvailabilityForTest(desk, true, from, until);

        assertThat(desk.getIsTemporarilyAvailable()).isTrue();
        assertThat(desk.getTemporaryAvailableFrom()).isEqualTo(from);
        assertThat(desk.getTemporaryAvailableUntil()).isEqualTo(until);
    }

    @Test
    @DisplayName("Should throw exception when temporary availability end date is in past")
    void applyTemporaryAvailability_ShouldThrowException_WhenEndDateInPast() {
        Desk desk = new Desk();
        LocalDateTime from = LocalDateTime.now().minusDays(5);
        LocalDateTime until = LocalDateTime.now().minusDays(1);

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.applyTemporaryAvailabilityForTest(desk, true, from, until),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusOverride()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("End date cannot be in the past");
    }

    @Test
    @DisplayName("Should throw exception when start date after end date")
    void applyTemporaryAvailability_ShouldThrowException_WhenStartAfterEnd() {
        Desk desk = new Desk();
        LocalDateTime from = LocalDateTime.now().plusDays(5);
        LocalDateTime until = LocalDateTime.now().plusDays(1);

        ExceptionResponse exception = catchThrowableOfType(
                () -> adminService.applyTemporaryAvailabilityForTest(desk, true, from, until),
                ExceptionResponse.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("INVALID_DATE_RANGE");
        assertThat(exception.getMessage()).contains("Start date cannot be after end date");
    }
}