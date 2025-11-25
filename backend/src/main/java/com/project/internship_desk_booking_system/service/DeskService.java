package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DeskColorDTO;
import com.project.internship_desk_booking_system.dto.DeskCoordinatesDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.enums.DeskColor;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for managing desk metadata within the booking system.
 *
 * This service provides methods to:
 * - Retrieve desk coordinates for visualization or mapping
 * - Get desks by color status (Gray, Blue) depending on their type, status, and temporary availability
 * - Retrieve lists of desk statuses and types
 *
 * The service handles exceptions when desks or coordinates are not found and
 * transforms Desk entities into DeskColorDTO or DeskCoordinatesDTO for frontend or API usage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeskService {

    private final DeskRepository deskRepository;

    /**
     * Retrieves all current desk coordinates from the system.
     *
     * @return a list of DeskCoordinatesDTO objects representing desk positions
     * @throws ExceptionResponse if no coordinates are found
     */
    public List<DeskCoordinatesDTO> getCoordinates() {
        List<DeskCoordinatesDTO> coordinates = deskRepository.findCurrentCoordinates();
        if (coordinates.isEmpty()) {
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "CURRENT_DESK_COORDINATES_NOT_FOUND",
                    "Current coordinates not found"
            );
        }
        return coordinates;
    }

    /**
     * Retrieves desks that should be displayed as Gray.
     * This includes desks that are deactivated or marked as unavailable and not temporarily available.
     *
     * @param localDate the reference date to check temporary availability
     * @return a list of DeskColorDTO representing Gray desks
     */
    public List<DeskColorDTO> getGrayDesks(LocalDate localDate) {
        List<Desk> deactivatedDesks = getDesksByStatus(DeskStatus.DEACTIVATED);

        List<Desk> unavailableDesks = getDesksByType(DeskType.UNAVAILABLE).stream()
                .filter(d -> d.getStatus() != DeskStatus.DEACTIVATED)
                .toList();

        List<Desk> allGrayDesks = new ArrayList<>();
        allGrayDesks.addAll(deactivatedDesks);
        allGrayDesks.addAll(unavailableDesks);

        return toDeskColorDTOCheckAvailability(
                allGrayDesks,
                DeskColor.GRAY,
                localDate
        );
    }

    /**
     * Retrieves desks that should be displayed as Blue (assigned desks).
     *
     * @param localDate the reference date to check temporary availability
     * @return a list of DeskColorDTO representing Blue desks
     */
    public List<DeskColorDTO> getBlueDesks(LocalDate localDate) {
        List<Desk> desks = getDesksByType(DeskType.ASSIGNED);

        return toDeskColorDTOCheckAvailability(
                desks,
                DeskColor.BLUE,
                localDate
        );
    }

    /**
     * Retrieves desks filtered by a specific desk type.
     *
     * @param deskType the DeskType to filter by
     * @return a list of Desk entities matching the desk type
     * @throws ExceptionResponse if no desks are found for the given type
     */
    private List<Desk> getDesksByType(DeskType deskType) {
        log.info("Looking for desks with DeskType {}", deskType);
        List<Desk> desks = deskRepository.findByType(deskType);

        if (desks == null || desks.isEmpty()) {
            log.warn("Desks with DeskType {} not found", deskType);
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    String.format("%s_DESKS_NOT_FOUND", deskType),
                    String.format("%s desks are not found", deskType)
            );
        }
        return desks;
    }

    /**
     * Retrieves desks filtered by a specific desk status.
     *
     * @param status the DeskStatus to filter by
     * @return a list of Desk entities matching the desk status
     * @throws ExceptionResponse if no desks are found for the given status
     */
    private List<Desk> getDesksByStatus(DeskStatus status) {
        log.info("Looking for desks with DeskStatus {}", status);
        List<Desk> desks = deskRepository.findByStatus(status);

        if (desks == null || desks.isEmpty()) {
            log.warn("Desks with DeskStatus {} not found", status);
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    String.format("%s_DESKS_NOT_FOUND", status),
                    String.format("%s desks are not found", status)
            );
        }
        return desks;
    }

    /**
     * Converts a list of Desk entities into DeskColorDTO objects, checking temporary availability
     * to determine the appropriate DeskColor (default or Green if temporarily available).
     *
     * @param desks the list of Desk entities to convert
     * @param defaultDeskColor the default DeskColor to assign
     * @param localDate the reference date for temporary availability
     * @return a list of DeskColorDTO objects
     */
    private List<DeskColorDTO> toDeskColorDTOCheckAvailability(
            List<Desk> desks,
            DeskColor defaultDeskColor,
            LocalDate localDate
    ) {
        List<DeskColorDTO> resultList = new ArrayList<>();

        for (Desk desk : desks) {
            DeskColorDTO deskColorDTO = new DeskColorDTO();
            deskColorDTO.setDeskId(desk.getId());

            if (desk.getIsTemporarilyAvailable() && desk.getTemporaryAvailableUntil() != null) {
                if (localDate.isBefore(desk.getTemporaryAvailableUntil().toLocalDate())) {
                    deskColorDTO.setDeskColor(DeskColor.GREEN);
                }
                if (deskColorDTO.getDeskColor() == null) {
                    deskColorDTO.setDeskColor(defaultDeskColor);
                }
            } else {
                deskColorDTO.setDeskColor(defaultDeskColor);
            }
            resultList.add(deskColorDTO);
        }
        return resultList;
    }

    /**
     * Retrieves all available desk statuses as strings.
     *
     * @return a list of desk status names
     */
    public List<String> getAllStatusDeskEnum() {
        return Arrays.stream(DeskStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all available desk types as strings.
     *
     * @return a list of desk type names
     */
    public List<String> getAllTypeDeskEnum() {
        return Arrays.stream(DeskType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
