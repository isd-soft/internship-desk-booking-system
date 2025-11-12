package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DeskColorDTO;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskColor;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeskService {

    private final DeskRepository deskRepository;
    private final DeskMapper deskMapper;


    @Transactional(readOnly = true)
    public List<DeskDto> getAllUnavailableDesks() {
        return deskRepository.findByType(DeskType.UNAVAILABLE)
                .stream()
                .map(deskMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeskDto> getAllAvailableDesks() {
        return deskRepository.findByStatus(DeskStatus.ACTIVE)
                .stream()
                .map(deskMapper::toDto)
                .toList();
    }

    public List<DeskCoordinatesDTO> getCoordinates(){
        List<DeskCoordinatesDTO>  coordinates = deskRepository.findCurrentCoordinates();
        if(coordinates.isEmpty()){
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "CURRENT_DESK_COORDINATES_NOT_FOUND",
                    "Current coordinates not found "
            );
        }
        return coordinates;
    }

    public List<DeskColorDTO> getGrayDesks(){
        List<Desk> desks = getDesksByType(DeskType.UNAVAILABLE);



        return toDeskColorDTO(
                desks,
                DeskColor.GRAY
        );
    }

    public List<DeskColorDTO> getBlueDesks(){
        List<Desk> desks = getDesksByType(DeskType.ASSIGNED);

        return toDeskColorDTO(
                desks,
                DeskColor.BLUE
        );
    }

    private List<Desk> getDesksByType(DeskType deskType){
        log.info(
                "Looking for desks with DeskType {}",
                deskType
        );
        List<Desk> desks = deskRepository
                .findByType(deskType);

        if(desks == null || desks.isEmpty()){
            log.warn(
                    "Desks with DeskType {} not found",
                    deskType
            );
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    String.format("%s_DESKS_NOT_FOUND", deskType),
                    String.format("%s desks are not found", deskType)
            );
        }
        return desks;
    }

    private List<DeskColorDTO> toDeskColorDTO(
            List<Desk> desks,
            DeskColor deskColor){
        return desks
                .stream()
                .map(
                        desk -> new DeskColorDTO(
                                desk.getId(),
                                deskColor))
                .toList();
    }
}
