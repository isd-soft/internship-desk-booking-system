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

/**
 * Service that provides availability information for a desk on a given date.
 * <p>
 * It composes working day boundaries from {@link BookingProperties}, queries
 * {@link BookingRepository} for existing bookings and maps them to DTOs using
 * {@link BookingAvailabilityMapper}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingAvailabilityService {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;
    private final BookingAvailabilityMapper mapper;

    /**
     * Returns availability information for the desk on the requested date.
     *
     * @param deskId id of the desk to check
     * @param date   date for which availability is requested
     * @return {@link DeskAvailabilityResponse} containing workday bounds and busy intervals
     */
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

    /**
     * Create workday start LocalDateTime for the given date using configured office start hour.
     *
     * @param date date to create start time for
     * @return LocalDateTime representing the start of the workday
     */
    private LocalDateTime createWorkdayStart(LocalDate date) {
        return date.atTime(bookingProperties.getOfficeStartHour(), 0);
    }

    /**
     * Create workday end LocalDateTime for the given date using configured office end hour.
     *
     * @param date date to create end time for
     * @return LocalDateTime representing the end of the workday
     */
    private LocalDateTime createWorkdayEnd(LocalDate date) {
        return date.atTime(bookingProperties.getOfficeEndHour(), 0);
    }

    /**
     * Collects busy intervals (as DTOs) for the desk between workdayStart and workdayEnd.
     *
     * @param deskId       id of the desk
     * @param workdayStart beginning of the day window
     * @param workdayEnd   end of the day window
     * @return list of {@link TimeIntervalDto} representing busy intervals; empty list when none
     */
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
