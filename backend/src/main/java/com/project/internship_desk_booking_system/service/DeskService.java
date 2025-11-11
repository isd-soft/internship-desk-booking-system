package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.dto.DeskDto;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

}
