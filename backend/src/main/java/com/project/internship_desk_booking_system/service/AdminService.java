package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;



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

    public DeskDTO addDesk(
            DeskDTO deskDto
    ) {
        log.info(
                "Adding new desk: name={}, zone={}, type={}, status={}",
                deskDto.deskName(),
                deskDto.zone(),
                deskDto.deskType(),
                deskDto.deskStatus()
        );

        Desk desk = new Desk();
        desk.setDeskName(deskDto.deskName());
        desk.setZone(deskDto.zone());

        desk.setType(
                deskDto.deskType() == null ?
                        DeskType.SHARED
                        : deskDto.deskType()
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

        deskRepository.save(desk);

        log.info("Desk saved successfully with id {}", desk.getId());

        return new DeskDTO(
                desk.getId(),
                desk.getDeskName(),
                desk.getZone(),
                desk.getType(),
                desk.getStatus(),
                desk.getIsTemporarilyAvailable(),
                desk.getTemporaryAvailableFrom(),
                desk.getTemporaryAvailableUntil()
        );
    }

    //TODO Prevent deactivation of desks that are currently occupied
    @Transactional
    public DeskDTO deactivateDesk(
            Long id
    ) {
        log.info("Deactivating desk with id {}", id);

        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id: " + id + " not found"));

        desk.setIsTemporarilyAvailable(false);
        desk.setStatus(DeskStatus.DEACTIVATED);

        deskRepository.save(desk);

        log.info("Desk {} deactivated successfully", id);

        return new DeskDTO(
                desk.getId(),
                desk.getDeskName(),
                desk.getZone(),
                desk.getType(),
                desk.getStatus(),
                desk.getIsTemporarilyAvailable(),
                desk.getTemporaryAvailableFrom(),
                desk.getTemporaryAvailableUntil()
        );
    }

    @Transactional
    public DeskDTO editDesk(
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

        if (updates.deskName() != null) {
            desk.setDeskName(updates.deskName());
        }
        if (updates.zone() != null) {
            desk.setZone(updates.zone());
        }
        if (updates.deskType() != null) {
            desk.setType(updates.deskType());
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

        deskRepository.save(desk);

        log.info("Desk {} updated successfully", id);

        return new DeskDTO(
                desk.getId(),
                desk.getDeskName(),
                desk.getZone(),
                desk.getType(),
                desk.getStatus(),
                desk.getIsTemporarilyAvailable(),
                desk.getTemporaryAvailableFrom(),
                desk.getTemporaryAvailableUntil()
        );
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

    private boolean hasActiveBookings(Desk desk) {
        return bookingRepository.existsActiveBookingsByDeskId(desk.getId(), LocalDateTime.now());

    }
    public List<DeskDTO> getAllDesks(){
        List<Desk> desks = deskRepository.findAll();
        List<DeskDTO> deskDTOList = new ArrayList<>();
        for(Desk desk : desks){
            DeskDTO deskDTO = new DeskDTO(
                    desk.getId(),
                    desk.getDeskName(),
                    desk.getZone(),
                    desk.getType(),
                    desk.getStatus(),
                    desk.getIsTemporarilyAvailable(),
                    desk.getTemporaryAvailableFrom(),
                    desk.getTemporaryAvailableUntil()
            );
            deskDTOList.add(deskDTO);
        }
        return deskDTOList;
    }
}
