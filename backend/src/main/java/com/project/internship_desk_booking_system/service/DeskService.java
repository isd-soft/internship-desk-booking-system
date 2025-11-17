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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeskService {

    private final DeskRepository deskRepository;

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

    public List<DeskColorDTO> getGrayDesks(
            LocalDate localDate
    ){
        List<Desk> desks = getDesksByType(DeskType.UNAVAILABLE);

        return toDeskColorDTOCheckAvailability(
                desks,
                DeskColor.GRAY,
                localDate
        );
    }

    public List<DeskColorDTO> getBlueDesks(
            LocalDate localDate
    ){
        List<Desk> desks = getDesksByType(DeskType.ASSIGNED);

        return toDeskColorDTOCheckAvailability(
                desks,
                DeskColor.BLUE,
                localDate
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

    private List<DeskColorDTO> toDeskColorDTOCheckAvailability(
            List<Desk> desks,
            DeskColor defaultDeskColor,
            LocalDate localDate
    ){

        List<DeskColorDTO> resultList = new ArrayList<>();

        for(Desk desk : desks){
            DeskColorDTO deskColorDTO = new DeskColorDTO();
            deskColorDTO.setDeskId(desk.getId());

            if(desk.getIsTemporarilyAvailable()){
                if(localDate.isBefore(desk.getTemporaryAvailableUntil().toLocalDate())){
                    deskColorDTO.setDeskColor(DeskColor.GREEN);
                }
                if(deskColorDTO.getDeskColor() == null){
                    deskColorDTO.setDeskColor(defaultDeskColor);
                }
            } else {
                deskColorDTO.setDeskColor(defaultDeskColor);
            }
            resultList.add(deskColorDTO);
        }
        return resultList;
    }
}
