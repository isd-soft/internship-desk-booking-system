package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.crossstore.ChangeSetPersister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final DeskRepository deskRepository;

    private void applyTemporaryAvailability(
            Desk desk,
            Boolean isTemporarilyAvailable,
            LocalDateTime from,
            LocalDateTime until
    ){
        boolean enabled = Boolean.TRUE.equals(isTemporarilyAvailable);
        desk.setIsTemporarilyAvailable(enabled);
        if(enabled){
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
        Desk desk = deskRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        desk.setIsTemporarilyAvailable(false);
        desk.setStatus(DeskStatus.DEACTIVATED);

        deskRepository.save(desk);
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
    ) throws ChangeSetPersister.NotFoundException {
        Desk desk = deskRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

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
        /*TODO For front dont show the option to set the temporary
            availability time if isTemporarilyAvailable is false
        */

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
        deskRepository.findById(id).ifPresent(desk -> deskRepository.deleteById(id));
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
