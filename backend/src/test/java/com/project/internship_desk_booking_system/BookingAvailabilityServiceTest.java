package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.config.BookingProperties;
import com.project.internship_desk_booking_system.dto.DeskAvailabilityResponse;
import com.project.internship_desk_booking_system.dto.TimeIntervalDto;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.mapper.BookingAvailabilityMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.service.BookingAvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingAvailabilityServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingProperties bookingProperties;

    @Mock
    private BookingAvailabilityMapper mapper;

    @InjectMocks
    private BookingAvailabilityService service;

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void getDeskAvailability_noBookings_returnsResponseAndComputedTimes() {
        Long deskId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 10);

        when(bookingProperties.getOfficeStartHour()).thenReturn(9);
        when(bookingProperties.getOfficeEndHour()).thenReturn(18);

        when(bookingRepository.findDeskBookingsForDay(eq(deskId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        DeskAvailabilityResponse expectedResponse = mock(DeskAvailabilityResponse.class);
        when(mapper.toDeskAvailabilityResponse(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(expectedResponse);

        DeskAvailabilityResponse actual = service.getDeskAvailability(deskId, date);

        assertSame(expectedResponse, actual);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);

        verify(mapper, times(1)).toDeskAvailabilityResponse(startCaptor.capture(), endCaptor.capture(), listCaptor.capture());

        assertEquals(date.atTime(9, 0), startCaptor.getValue());
        assertEquals(date.atTime(18, 0), endCaptor.getValue());
        assertTrue(listCaptor.getValue().isEmpty());
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void getDeskAvailability_withBookings_mapsIntervals() {
        Long deskId = 2L;
        LocalDate date = LocalDate.of(2025, 6, 5);

        when(bookingProperties.getOfficeStartHour()).thenReturn(8);
        when(bookingProperties.getOfficeEndHour()).thenReturn(17);

        Booking bookingEntity = mock(Booking.class);
        when(bookingRepository.findDeskBookingsForDay(eq(deskId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(bookingEntity));

        TimeIntervalDto intervalDto = mock(TimeIntervalDto.class);
        when(mapper.toTimeIntervalDto(any())).thenReturn(intervalDto);

        DeskAvailabilityResponse expectedResponse = mock(DeskAvailabilityResponse.class);
        when(mapper.toDeskAvailabilityResponse(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(expectedResponse);

        DeskAvailabilityResponse actual = service.getDeskAvailability(deskId, date);

        assertSame(expectedResponse, actual);

        verify(mapper, times(1)).toTimeIntervalDto(bookingEntity);

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mapper, times(1)).toDeskAvailabilityResponse(any(LocalDateTime.class), any(LocalDateTime.class), listCaptor.capture());

        List capturedList = listCaptor.getValue();
        assertEquals(1, capturedList.size());
        assertSame(intervalDto, capturedList.get(0));
    }
}
