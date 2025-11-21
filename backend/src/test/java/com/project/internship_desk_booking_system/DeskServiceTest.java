package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.ZoneDto;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.ZoneRepository;
import com.project.internship_desk_booking_system.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


public class DeskServiceTest {
    private static final LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime endTime = LocalDateTime.now().plusDays(1).plusHours(8);

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private DeskMapper deskMapper;

    @InjectMocks
    private AdminService adminService;

    private Desk desk;
    private User user;
    private Booking booking;
    private Zone zone;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("john@example.com", "hash");
        user.setId(1L);

        zone = new Zone();
        zone.setId(1L);
        zone.setZoneAbv("SER");
        zone.setZoneName("Service");

        desk = new Desk();
        desk.setId(5L);
        desk.setStatus(DeskStatus.ACTIVE);
        desk.setZone(zone);
        desk.setCurrentX(50.0);
        desk.setCurrentY(75.0);
        desk.setBaseX(50.0);
        desk.setBaseY(75.0);
        desk.setHeight(120L);
        desk.setWidth(60L);

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
        assertEquals(DeskStatus.ACTIVE, result.deskStatus());
        assertTrue(result.isTemporarilyAvailable());

        verify(deskRepository, times(1)).save(any(Desk.class));
        verify(zoneRepository, times(1)).findById(1L);
    }

    @Test
    void deactivateDesk_success() {
        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));
        when(deskMapper.toDto(any(Desk.class)))
                .thenAnswer(invocation -> {
                    Desk d = invocation.getArgument(0);

                    return new DeskDto(
                            d.getId(),
                            d.getDeskName(),
                            new ZoneDto(d.getZone().getId(), d.getZone().getZoneName(), d.getZone().getZoneAbv()),
                            d.getType(),
                            d.getStatus(),
                            d.getIsTemporarilyAvailable(),
                            null,
                            null,
                            d.getCurrentX(),
                            d.getCurrentY(),
                            d.getBaseX(),
                            d.getBaseY(),
                            d.getHeight(),
                            d.getWidth(),
                            null
                    );
                });

        var result = adminService.deactivateDesk(desk.getId());

        assertEquals(DeskStatus.DEACTIVATED, result.deskStatus());
        assertFalse(result.isTemporarilyAvailable());

        verify(deskRepository, times(1)).save(any(Desk.class));
    }

    private ZoneDto createZoneDTO() {
        return new ZoneDto(1L, "Service", "SER");
    }

    private DeskDto createDeskDTO() {
        return new DeskDto(
                5L,
                "Desk-A1",
                createZoneDTO(),
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                true,
                null, null, 50.0, 75.0, 50.0, 75.0, 120L, 60L, "Out of Service"
        );
    }
    @Test
    void applyTemporaryAvailability_enable_success() {
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime until = LocalDateTime.now().plusDays(2);

        adminService.applyTemporaryAvailabilityForTest(desk, true, from, until);

        assertTrue(desk.getIsTemporarilyAvailable());
        assertEquals(from, desk.getTemporaryAvailableFrom());
        assertEquals(until, desk.getTemporaryAvailableUntil());
        System.out.println("Desk: " +desk.getDeskName()+" was set TemporarilyAvailable "+desk.getIsTemporarilyAvailable()+ " from "+
                desk.getTemporaryAvailableFrom()+" until "+desk.getTemporaryAvailableUntil());
    }

    @Test
    void applyTemporaryAvailability_enable_invalidRange() {

        LocalDateTime from = LocalDateTime.now().plusDays(3);
        LocalDateTime until = LocalDateTime.now().plusDays(1);

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.applyTemporaryAvailabilityForTest(desk, true, from, until));

        assertEquals("INVALID_DATE_RANGE", ex.getCode(), ex.getMessage());
        System.out.println("Caught exception: " + ex.getMessage());
    }

    @Test
    void applyTemporaryAvailability_disable() {
        adminService.applyTemporaryAvailabilityForTest(desk, false, null, null);

        assertFalse(desk.getIsTemporarilyAvailable());
        assertNull(desk.getTemporaryAvailableFrom());
        assertNull(desk.getTemporaryAvailableUntil());
    }


    @Test
    void saveAllDesks_updatesExistingDesk() {
        DeskDto deskDTO = createDeskDTO();
        when(deskRepository.findById(deskDTO.id())).thenReturn(Optional.of(desk));
        when(zoneRepository.findById(anyLong())).thenReturn(Optional.of(zone));

        int updated = adminService.saveAllDesks(List.of(deskDTO));

        assertEquals(1, updated);
        verify(deskRepository, times(1)).save(any(Desk.class));
    }

    @Test
    void saveAllDesks_createsNewDesk() {
        DeskDto deskDTO = createDeskDTO();
        when(deskRepository.findById(deskDTO.id())).thenReturn(Optional.empty());
        when(zoneRepository.findById(anyLong())).thenReturn(Optional.of(zone));

        int updated = adminService.saveAllDesks(List.of(deskDTO));

        assertEquals(1, updated);
        verify(deskRepository, times(1)).save(any(Desk.class));
    }

    @Test
    void changeCurrentCoordinates_success() {
        System.out.println("Initial coordinates for Desk: " +desk.getId()+" :currentX=  "+desk.getCurrentX()+ ", currentY= "+ desk.getCurrentY());
        CoordinatesUpdateCommand cmd = new CoordinatesUpdateCommand(desk.getId(), 100.0, 200.0);
        when(deskRepository.findById(desk.getId())).thenReturn(Optional.of(desk));

        DeskCoordinatesDTO result = adminService.changeCurrentCoordinates(cmd);

        assertEquals(100.0, result.x());
        assertEquals(200.0, result.y());
        verify(deskRepository, times(1)).save(desk);
        System.out.println("Changed coordinates for Desk: " +desk.getId()+" to new  currentX=  "+result.x()
                + ", currentY= "+ result.y());
    }

    @Test
    void changeCurrentCoordinates_notFound() {
        CoordinatesUpdateCommand cmd = new CoordinatesUpdateCommand(99L, 100.0, 200.0);
        when(deskRepository.findById(anyLong())).thenReturn(Optional.empty());

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.changeCurrentCoordinates(cmd));
        assertEquals("DESK_NOT_FOUND", ex.getCode());
        System.out.println("Caught exception: " + ex.getMessage());
    }

}