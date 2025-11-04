package com.project.internship_desk_booking_system.admin;

import com.project.internship_desk_booking_system.DTO.DeskDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.DeskStatus;
import com.project.internship_desk_booking_system.entity.DeskType;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class AdminService {
    private final DeskRepository deskRepository;

    public AdminService(
        DeskRepository deskRepository
    ){
        this.deskRepository = deskRepository;
    }
    public Desk addDesk(
            DeskDTO deskDto
    ){
        Desk desk = new Desk();
        desk.setDeskName(deskDto.deskName());
        desk.setFloor(deskDto.floor());
        desk.setZone(deskDto.zone());

        desk.setType(
                deskDto.deskType() == null ?
                        DeskType.SHARED
                        :deskDto.deskType()
        );
        desk.setStatus(
                deskDto.deskStatus() == null ?
                        DeskStatus.ACTIVE
                        :deskDto.deskStatus()
        );
        desk.setIsTemporarilyAvailable(true);
        desk.setTemporaryAvailableFrom(LocalDateTime.now());
        desk.setTemporaryAvailableUntil(
                LocalDateTime
                        .now()
                        .plusDays(20)
        );

        return deskRepository
                .save(desk);
    }

    @Transactional
    public Desk deactivateDesk(
            Long id
    ){
        Desk desk = deskRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        desk.setIsTemporarilyAvailable(false);

        return deskRepository.save(desk);
    }

    public Desk editDesk(
            Desk desk
    ) throws ChangeSetPersister.NotFoundException {
        Desk oldDesk = deskRepository.findById(
                desk.getId())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        oldDesk.setDeskName(desk.getDeskName());
        oldDesk.setFloor(desk.getFloor());
        oldDesk.setZone(desk.getZone());
        oldDesk.setType(desk.getType());
        oldDesk.setStatus(desk.getStatus());
        oldDesk.setIsTemporarilyAvailable(desk.getIsTemporarilyAvailable());
        oldDesk.setTemporaryAvailableFrom(desk.getTemporaryAvailableFrom());
        oldDesk.setTemporaryAvailableUntil(desk.getTemporaryAvailableUntil());

        return deskRepository.save(oldDesk);
    }

    public void deleteDesk(
            Long id
    ){
        deskRepository.deleteById(id);
    }
}
