package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.config.BookingProperties;
import com.project.internship_desk_booking_system.dto.DeskAvailabilityResponse;
import com.project.internship_desk_booking_system.dto.TimeIntervalDto;
import com.project.internship_desk_booking_system.mapper.BookingAvailabilityMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingAvailabilityService {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;
    private final BookingAvailabilityMapper mapper;

    @Transactional(readOnly = true)
    public DeskAvailabilityResponse getDeskAvailability(Long deskId, LocalDate date) {
        LocalDateTime workdayStart = createWorkdayStart(date);
        LocalDateTime workdayEnd = createWorkdayEnd(date);

        List<TimeIntervalDto> busyIntervals = collectBusyIntervals(
                deskId,
                workdayStart,
                workdayEnd
        );

        return mapper.toDeskAvailabilityResponse(workdayStart, workdayEnd, busyIntervals);
    }

    private LocalDateTime createWorkdayStart(LocalDate date) {
        return date.atTime(bookingProperties.getOfficeStartHour(), 0);
    }

    private LocalDateTime createWorkdayEnd(LocalDate date) {
        return date.atTime(bookingProperties.getOfficeEndHour(), 0);
    }

    private List<TimeIntervalDto> collectBusyIntervals(
            Long deskId,
            LocalDateTime workdayStart,
            LocalDateTime workdayEnd
    ) {
        List<TimeIntervalDto> result = new ArrayList<>();

        bookingRepository.findDeskBookingsForDay(deskId, workdayStart, workdayEnd)
                .stream()
                .map(mapper::toTimeIntervalDto)
                .forEach(result::add);


        return result;
    }

}


