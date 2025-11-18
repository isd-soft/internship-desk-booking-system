package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.dto.EmailRoleDTO;
import com.project.internship_desk_booking_system.dto.ZoneDto;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.mapper.ZoneMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;
    private final ZoneRepository zoneRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final DeskMapper deskMapper;
    private final BookingTimeLimitsService bookingTimeLimitsService;
    private final BookingServiceValidation bookingValidation;
    private final ZoneMapper zoneMapper;

    @Value("${app.default-admin-id}")
    private Long defaultAdminId;

    public void deleteUser(Long userId) {
        if (userId.equals(defaultAdminId)) {
            log.warn("Cannot delete default admin");
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "CANNOT_DELETE_DEFAULT_ADMIN", "Cannot delete default admin");
        }
        deskRepository.deleteById(userId);
    }

    private void applyTemporaryAvailability(
            Desk desk,
            Boolean isTemporarilyAvailable,
            LocalDateTime from,
            LocalDateTime until
    ) {
        boolean enabled = Boolean.TRUE.equals(isTemporarilyAvailable);
        desk.setIsTemporarilyAvailable(enabled);
        if (enabled) {
            if (from == null || until == null) {
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_DATE_RANGE",
                        "Start and end date must be provided when temporary availability is enabled"

                );
            }
            if (from.isAfter(until)) {
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_DATE_RANGE",
                        "Start date cannot be after end date"
                );
            }

            if (until.isBefore(LocalDateTime.now())) {
                log.warn("Temporary availability end date {} is in the past", until);
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_DATE_RANGE",
                        "End date cannot be in the past"
                );
            }

            desk.setTemporaryAvailableFrom(from);
            desk.setTemporaryAvailableUntil(until);
        } else {
            log.info("Desk {} is no longer temporarily available", desk.getId());
            desk.setTemporaryAvailableFrom(null);
            desk.setTemporaryAvailableUntil(null);
        }
    }

    public DeskDto getDeskById(
            Long deskId
    ) {
        Desk desk = deskRepository
                .findById(deskId).orElseThrow(
                        () -> {
                            log.warn(
                                    "Desk with id {} not found",
                                    deskId
                            );
                            return new ExceptionResponse(
                                    HttpStatus.NOT_FOUND,
                                    "DESK_NOT_FOUND",
                                    String.format("Desk with id %d not found", deskId)
                            );
                        });

        return deskMapper.toDto(desk);
    }

    public DeskDto addDesk(
            DeskDto deskDto
    ) {
        log.info(
                "Adding new desk: name={}, zone={}, type={}, status={}",
                deskDto.displayName(),
                deskDto.zoneDto().getId(),
                deskDto.type(),
                deskDto.deskStatus()
        );
        Zone zone = zoneRepository.findById(deskDto.zoneDto().getId())
                .orElseThrow(() -> new RuntimeException("Zone not found: " + deskDto.zoneDto()));
        Desk desk = new Desk();
        desk.setDeskName(deskDto.displayName());
        desk.setZone(zone);

        desk.setType(
                deskDto.type() == null ?
                        DeskType.SHARED
                        : deskDto.type()
        );
        desk.setStatus(
                deskDto.deskStatus() == null ?
                        DeskStatus.ACTIVE
                        : deskDto.deskStatus()
        );
        applyTemporaryAvailability(
                desk,
                deskDto.isTemporarilyAvailable(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(20)
        );
        desk.setCurrentX(
                deskDto.currentX() == null ?
                        null :
                        deskDto.currentX()
        );
        desk.setCurrentY(
                deskDto.currentY() == null ?
                        null :
                        deskDto.currentY()
        );
        desk.setBaseX(
                deskDto.baseX() == null ?
                        null :
                        deskDto.baseX()
        );
        desk.setBaseY(
                deskDto.baseY() == null ?
                        null :
                        deskDto.baseY()
        );
        desk.setHeight(
                deskDto.height() == null ?
                        null :
                        deskDto.height()
        );
        desk.setWidth(
                deskDto.width() == null ?
                        null :
                        deskDto.width()
        );

        deskRepository.save(desk);

        log.info("Desk saved successfully with id {}", desk.getId());

        return deskMapper.toDto(desk);
    }

    @Transactional
    public DeskDto deactivateDesk(
            Long id
    ) {
        log.info("Deactivating desk with id {}", id);

        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id: " + id + " not found"));

        desk.setIsTemporarilyAvailable(false);
        desk.setStatus(DeskStatus.DEACTIVATED);

        deskRepository.save(desk);

        log.info("Desk {} deactivated successfully", id);

        return deskMapper.toDto(desk);
    }

    public DeskDto activateDesk(
            Long id
    ) {
        log.info("Activating desk with id {}", id);

        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        "Desk with id: " + id + " not found"
                ));
        desk.setStatus(DeskStatus.ACTIVE);
        deskRepository.save(desk);
        log.info("Desk {} activated successfully", id);
        return deskMapper.toDto(desk);
    }

    @Transactional
    public DeskDto editDesk(
            Long id,
            DeskUpdateDTO updates
    ) {
        log.info("Editing desk with id {}", id);

        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        "Desk with id: " + id + " not found"
                ));

        if (updates.displayName() != null) {
            desk.setDeskName(updates.displayName());
        }
        if (updates.zoneId() != null) {
            Zone zone = zoneRepository.findById(updates.zoneId())
                    .orElseThrow(() -> new ExceptionResponse(
                            HttpStatus.NOT_FOUND,
                            "ZONE_NOT_FOUND",
                            "Zone not found: " + updates.zoneId()
                    ));
            desk.setZone(zone);
        }
        if (updates.type() != null) {
            desk.setType(updates.type());
        }
        if (updates.deskStatus() != null) {
            desk.setStatus(updates.deskStatus());
        }
        if (updates.isTemporarilyAvailable() != null) {
            applyTemporaryAvailability(
                    desk,
                    updates.isTemporarilyAvailable(),
                    updates.temporaryAvailableFrom(),
                    updates.temporaryAvailableUntil()
            );
        }
        if (updates.currentX() != null) {
            desk.setCurrentX(updates.currentX());
        }
        if (updates.currentY() != null) {
            desk.setCurrentY(updates.currentY());
        }
        if (updates.baseX() != null) {
            desk.setBaseX(updates.baseX());
        }
        if (updates.baseY() != null) {
            desk.setBaseY(updates.baseY());
        }
        if(updates.height() != null){
            desk.setHeight(updates.height());
        }
        if(updates.width() != null){
            desk.setWidth(updates.width());
        }

        deskRepository.save(desk);

        log.info("Desk {} updated successfully", id);

        return deskMapper.toDto(desk);
    }

// Update the deleteDesk method in AdminService.java

    @Transactional
    public void deleteDesk(Long id, String reason) {
        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        "Desk not found with id: " + id
                ));

        if (hasActiveBookings(desk)) {
            log.info("Desk {} has active bookings. Cancelling them before deletion.", id);
            bookingRepository.cancelAllActiveBookingsForDesk(id);
            log.info("All active bookings for desk {} have been cancelled", id);
        }
        if (hasScheduledBookings(desk)) {
            log.info("Desk {} has scheduled bookings. Cancelling them before deletion.", id);
            bookingRepository.cancelAllPendingBookingsForDesk(id);
            log.info("All scheduled bookings for desk {} have been cancelled", id);
        }

        desk.setIsDeleted(true);
        desk.setDeletedAt(LocalDateTime.now());
        desk.setReasonOfDeletion(reason != null && !reason.trim().isEmpty() ? reason.trim()
                : "No reason provided");

        deskRepository.save(desk);

        log.info("Desk {} soft deleted successfully with reason: {}", id, reason);
    }

    @Transactional
    public void restoreDesk(Long id) {
        Desk desk = deskRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        "Desk not found with id: " + id
                ));

        desk.setIsDeleted(false);
        desk.setDeletedAt(null);
        deskRepository.save(desk);

        log.info("Desk {} restored successfully", id);
    }

    public List<DeskDto>    getAllDesks(){
        List<Desk> desks = deskRepository.findAll();
        List<DeskDto> deskDtoList = new ArrayList<>();
        for (Desk desk : desks) {
            DeskDto deskDTO = deskMapper.toDto(desk);
            deskDtoList.add(deskDTO);
        }
        return deskDtoList;
    }

    public List<DeskCoordinatesDTO> getBaseCoordinates() {
        List<DeskCoordinatesDTO> coordinates = deskRepository.findBaseCoordinates();
        if (coordinates.isEmpty()) {
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "CURRENT_DESK_COORDINATES_NOT_FOUND",
                    "Current coordinates not found "
            );
        }
        return coordinates;
    }

    @Transactional
    public DeskCoordinatesDTO changeCurrentCoordinates(
            CoordinatesUpdateCommand coordinatesUpdateCommand
    ) {
        Desk desk = deskRepository
                .findById(coordinatesUpdateCommand.deskId())
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        String.format(
                                "Desk with id %d not found ",
                                coordinatesUpdateCommand.deskId()
                        )
                ));
        desk.setCurrentX(coordinatesUpdateCommand.currentX());
        desk.setCurrentY(coordinatesUpdateCommand.currentY());

        deskRepository.save(desk);

        return new DeskCoordinatesDTO(
                desk.getId(),
                desk.getDeskName(),
                desk.getCurrentX(),
                desk.getCurrentY(),
                desk.getHeight(),
                desk.getWidth()
        );
    }

    @Transactional
    public BookingResponse cancelBooking(
            Long bookingId
    ) {
        Booking booking = findBookingById(bookingId);
        log.info(
                "Cancelling booking with id {}",
                bookingId
        );
        booking.setStatus(BookingStatus.CANCELLED);

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse editBooking(
            Long bookingId,
            BookingUpdateCommand bookingUpdateCommand
    ) {
        log.info("Looking for a booking with id: {}", bookingId);

        Booking existingBooking = findBookingById(bookingId);

        Long newUserId = bookingUpdateCommand.userId();
        Long currentUserId = existingBooking.getUser().getId();

        LocalDateTime finalStartTime = bookingUpdateCommand.startTime() != null
                ? bookingUpdateCommand.startTime()
                : existingBooking.getStartTime();

        LocalDateTime finalEndTime = bookingUpdateCommand.endTime() != null
                ? bookingUpdateCommand.endTime()
                : existingBooking.getEndTime();

        boolean userIsChanging = newUserId != null && !newUserId.equals(currentUserId);
        if (userIsChanging) {
            List<Booking> userBookings = bookingRepository.findUserBookings(
                    newUserId,
                    finalStartTime,
                    finalEndTime
            );

            if (!userBookings.isEmpty()) {
                log.error("User id: {} already has {} booking(s) in the requested time period",
                        newUserId, userBookings.size());
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "USER_NOT_AVAILABLE",
                        "User already has a booking in this time period"
                );
            }
        }
        if (bookingUpdateCommand.startTime() != null) {
            if (finalStartTime.isBefore(LocalDateTime.now())) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE",
                        "Cannot start booking before current time");
            }

            BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();
            long daysInAdvance = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    finalStartTime.toLocalDate()
            );

            if (daysInAdvance > policy.getMaxDaysInAdvance()) {
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "BOOKING_TOO_FAR_AHEAD",
                        String.format(
                                "Cannot book more than %d days in advance. You are trying to book %d days ahead.",
                                policy.getMaxDaysInAdvance(), daysInAdvance
                        )
                );
            }
        }
        bookingValidation.validateOfficeHours(finalStartTime, finalEndTime);
        bookingValidation.validateBookingTimes(finalStartTime, finalEndTime);
        if (bookingUpdateCommand.endTime() != null) {
            if (finalEndTime.isBefore(finalStartTime)) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE",
                        "End time must be after start time");
            }
        }

        if (bookingUpdateCommand.deskId() != null) {
            Desk desk = deskRepository.findById(bookingUpdateCommand.deskId())
                    .orElseThrow(() -> new ExceptionResponse(
                            HttpStatus.NOT_FOUND,
                            "DESK_NOT_FOUND",
                            "Desk with id " + bookingUpdateCommand.deskId() + " not found"
                    ));
        }
        if (userIsChanging) {
            log.info("Changing user from {} to {} for booking {}", currentUserId, newUserId, bookingId);
            changeUser(newUserId, existingBooking);
        }

        if (bookingUpdateCommand.deskId() != null) {
            log.info("Editing desk with id {} for booking with id {}", bookingUpdateCommand.deskId(), bookingId);
            changeDesk(bookingUpdateCommand.deskId(), existingBooking);
        }

        existingBooking.setStartTime(finalStartTime);
        existingBooking.setEndTime(finalEndTime);

        if (bookingUpdateCommand.status() != null) {
            log.info("Editing status {} for booking with id {}", bookingUpdateCommand.status(), bookingId);
            existingBooking.setStatus(bookingUpdateCommand.status());
        }

        Booking saved = bookingRepository.save(existingBooking);
        return bookingMapper.toResponse(saved);
    }


    private boolean hasActiveBookings(Desk desk) {
        return bookingRepository.existsActiveBookingsByDeskId(desk.getId(), LocalDateTime.now());
    }

    private boolean hasScheduledBookings(Desk desk) {
        return bookingRepository.existsScheduledBookingsByDeskId(desk.getId(), LocalDateTime.now());
    }

    private Booking findBookingById(
            Long bookingId
    ) {
        return bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND",
                        String.format(
                                "Booking with id:{%d} not found",
                                bookingId
                        )
                ));
    }

    private void changeUser(
            Long userId,
            Booking booking
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        String.format(
                                "User with id: {%d} not found ",
                                userId)
                ));
        booking.setUser(user);
    }

    private void changeDesk(
            Long deskId,
            Booking booking
    ) {
        Desk desk = deskRepository
                .findById(deskId)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        String.format(
                                "Desk with id: {%d} not found",
                                deskId
                        )
                ));
        booking.setDesk(desk);
    }

    public List<DeskDto> getAllDeletedDesks() {
        List<Desk> deletedDesks = deskRepository.findAllDeleted();
        return deletedDesks.stream()
                .map(deskMapper::toDto)
                .toList();
    }

    public List<EmailRoleDTO> getAllRegisteredUserEmails() {
        log.info("Fetching all unique user emails from bookings");
        List<EmailRoleDTO> users = userRepository.findDistinctEmailAndRole();
        log.info("Found {} unique users", users.size());

        if (users.isEmpty()) {
            log.warn("No users were found");
        }

        return users;
    }

    public List<ZoneDto> getAllZones() {
        List<Zone> zones = zoneRepository.findAll();
        List<ZoneDto> zoneDtoList = new ArrayList<>();
        for(Zone zone : zones){
            ZoneDto zoneDTO = zoneMapper.toDto(zone);
            zoneDtoList.add(zoneDTO);
        }
        return zoneDtoList;
    }

}