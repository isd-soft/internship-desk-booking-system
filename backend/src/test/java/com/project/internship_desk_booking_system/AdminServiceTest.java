
package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AdminService adminService;

    private Desk desk;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        desk = new Desk();
        desk.setId(5L);
        desk.setStatus(DeskStatus.ACTIVE);
    }

    @Test
    void addDesk_success() {
        DeskDTO deskDTO = new DeskDTO(
                desk.getId(),
                "Desk-A1",
                "Zone-1",
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                false,
                null,
                null
        );


        when(deskRepository.save(any(Desk.class)))
                .thenReturn(desk);

        var result = adminService.addDesk(deskDTO);

        assertEquals(DeskType.SHARED, result.deskType());
        assertEquals(DeskStatus.ACTIVE, result.deskStatus());
        assertFalse(result.isTemporarilyAvailable());

        verify(deskRepository, times(1))
                .save(any(Desk.class));
    }

    @Test
    void editDesk_success() {
        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
                "Desk-Updated",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(deskRepository.findById(desk.getId()))
                .thenReturn(Optional.of(desk));

        adminService.editDesk(desk.getId(), updateDTO);

        verify(deskRepository, times(1))
                .findById(desk.getId());
        verify(deskRepository, times(1))
                .save(any(Desk.class));
    }

    @Test
    void deactivateDesk_success() {
        when(deskRepository.findById(desk.getId()))
                .thenReturn(Optional.of(desk));

        var result = adminService.deactivateDesk(desk.getId());

        assertEquals(DeskStatus.DEACTIVATED, result.deskStatus());
        assertFalse(result.isTemporarilyAvailable());

        verify(deskRepository, times(1))
                .findById(desk.getId());
        verify(deskRepository, times(1))
                .save(any(Desk.class));
    }

    @Test
    void deleteDesk_success() {
        when(deskRepository.findById(desk.getId()))
                .thenReturn(Optional.of(desk));
        when(bookingRepository.existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(false);

        adminService.deleteDesk(desk.getId());

        verify(deskRepository, times(1))
                .findById(desk.getId());
        verify(bookingRepository, times(1))
                .existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class));
        verify(deskRepository, times(1))
                .deleteById(desk.getId());
    }

    @Test
    void deleteDesk_throws_WhenHasActiveBookings() {
        when(deskRepository.findById(desk.getId()))
                .thenReturn(Optional.of(desk));
        when(bookingRepository.existsActiveBookingsByDeskId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(true);

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.deleteDesk(desk.getId()));
        assertEquals("DESK_HAS_ACTIVE_BOOKINGS", ex.getCode());

        verify(deskRepository, never())
                .deleteById(desk.getId());
    }

    @Test
    void editDesk_throws_WhenStartDateAfterEndDate() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
                null,
                null,
                null,
                null,
                true,
                start,
                end
        );

        when(deskRepository.findById(desk.getId()))
                .thenReturn(Optional.of(desk));

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editDesk(desk.getId(), updateDTO));
        assertEquals("INVALID_DATE_RANGE", ex.getCode());
    }

    @Test
    void editDesk_throws_WhenEndDateInPast() {
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now().minusDays(5);

        DeskUpdateDTO updateDTO = new DeskUpdateDTO(
                null,
                null,
                null,
                null,
                true,
                start,
                end
        );

        when(deskRepository.findById(desk.getId()))
                .thenReturn(Optional.of(desk));

        ExceptionResponse ex = assertThrows(ExceptionResponse.class,
                () -> adminService.editDesk(desk.getId(), updateDTO));
        assertEquals("INVALID_DATE_RANGE", ex.getCode());
    }
}