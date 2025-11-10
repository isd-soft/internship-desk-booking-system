package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.Zone;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.ZoneRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    private void applyTemporaryAvailability(
            Desk desk,
            Boolean isTemporarilyAvailable,
            LocalDateTime from,
            LocalDateTime until
    ){
        boolean enabled = Boolean.TRUE.equals(isTemporarilyAvailable);
        desk.setIsTemporarilyAvailable(enabled);
        if(enabled){
            if(from.isAfter(until)){
                log.warn(
                        "Temporary availability start date {} is after end date {}",
                        from,
                        until
                );
                throw new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_DATE_RANGE",
                        "Start date cannot be after end date"
                );
            }

            if(until.isBefore(LocalDateTime.now())){
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
            desk.setTemporaryAvailableFrom(null);
            desk.setTemporaryAvailableUntil(null);
        }
    }
    public DeskDto addDesk(
            DeskDto deskDto
    ) {
        log.info(
                "Adding new desk: name={}, zone={}, type={}, status={}",
                deskDto.displayName(),
                deskDto.zoneId(),
                deskDto.type(),
                deskDto.deskStatus()
        );
        Zone zone = zoneRepository.findById(deskDto.zoneId())
                .orElseThrow(() -> new RuntimeException("Zone not found: " + deskDto.zoneId()));
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
        if (updates.zone() != null) {
            Zone zone = zoneRepository.findById(updates.zone())
                    .orElseThrow(() -> new ExceptionResponse(
                            HttpStatus.NOT_FOUND,
                            "ZONE_NOT_FOUND",
                            "Zone not found: " + updates.zone()
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
                    updates.temporaryAvailableFrom() != null ?
                            updates.temporaryAvailableFrom() : LocalDateTime.now(),
                    updates.temporaryAvailableUntil() != null ?
                            updates.temporaryAvailableUntil() : LocalDateTime.now().plusDays(20)
            );
        }
        if(updates.currentX() != null){
            desk.setCurrentX(updates.currentX());
        }
        if(updates.currentY() != null){
            desk.setCurrentY(updates.currentY());
        }
        if(updates.baseX() != null){
            desk.setBaseX(updates.baseX());
        }
        if(updates.baseY() != null){
            desk.setBaseY(updates.baseY());
        }

        deskRepository.save(desk);

        log.info("Desk {} updated successfully", id);

        return deskMapper.toDto(desk);
    }

    public void deleteDesk(
            Long id
    ){
        log.info("Deleting desk with id {}", id);

        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        "Desk with id: " + id + " not found"
                ));

        // Check if desk has active bookings
        if (hasActiveBookings(desk)) {
            log.warn("Cannot delete desk {} - has active bookings", id);
            throw new ExceptionResponse(
                    HttpStatus.CONFLICT,
                    "DESK_HAS_ACTIVE_BOOKINGS",
                    "Cannot delete desk with active bookings"
            );
        }

        deskRepository.deleteById(id);
        log.info("Desk {} deleted successfully", id);
    }

    public List<DeskDto> getAllDesks(){
        List<Desk> desks = deskRepository.findAll();
        List<DeskDto> deskDtoList = new ArrayList<>();
        for(Desk desk : desks){
            DeskDto deskDTO = deskMapper.toDto(desk);
            deskDtoList.add(deskDTO);
        }
        return deskDtoList;
    }

    public List<DeskCoordinatesDTO> getBaseCoordinates(){
        List<DeskCoordinatesDTO> coordinates = deskRepository.findBaseCoordinates();
        if(coordinates.isEmpty()){
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
    ){
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
                desk.getCurrentX(),
                desk.getCurrentY()
        );
    }

   @Transactional
    public BookingResponse cancelBooking(
        Long bookingId
    ){
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
    ){
        log.info("Looking for a booking with id:{{}}", bookingId);

        Booking existingBooking = findBookingById(bookingId);

        if(bookingUpdateCommand.userId() != null){
            log.info(
                    "Editing user with id {} for booking with id {}",
                    bookingUpdateCommand.userId(),
                    bookingId
            );
            changeUser(
                    bookingUpdateCommand.userId(),
                    existingBooking);
        }
        if(bookingUpdateCommand.deskId() != null){
            log.info(
                    "Editing desk with id {} for booking with id {}",
                    bookingUpdateCommand.deskId(),
                    bookingId
            );
            changeDesk(
                    bookingUpdateCommand.deskId(),
                    existingBooking);
        }
        if(bookingUpdateCommand.startTime() != null){
            log.info(
                    "Editing start time {} for booking with id {}",
                    bookingUpdateCommand.startTime(),
                    bookingId

            );
            existingBooking.setStartTime(bookingUpdateCommand.startTime());
        }
        if(bookingUpdateCommand.endTime() != null){
            log.info(
                    "Editing end time {} for booking with id {}",
                    bookingUpdateCommand.endTime(),
                    bookingId

            );
            existingBooking.setEndTime(bookingUpdateCommand.endTime());
        }
        if(bookingUpdateCommand.status() != null){
            log.info(
                    "Editing status {} for booking with id {}",
                    bookingUpdateCommand.status(),
                    bookingId

            );
            existingBooking.setStatus(bookingUpdateCommand.status());
        }

        return  bookingMapper.toResponse(existingBooking);
    }
    private boolean hasActiveBookings(Desk desk) {
        return bookingRepository.existsActiveBookingsByDeskId(desk.getId(), LocalDateTime.now());

    }

    private Booking findBookingById(
            Long bookingId
    ){
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
    ){
        User user = userRepository
                .findById(userId)
                .orElseThrow(()-> new ExceptionResponse(
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
    ){
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
}
