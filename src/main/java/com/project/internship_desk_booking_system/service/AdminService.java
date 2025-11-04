package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.DTO.DeskDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.crossstore.ChangeSetPersister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final DeskRepository deskRepository;

    public Desk addDesk(
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
    ) {
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
    ) {
        deskRepository.deleteById(id);
    }
}
