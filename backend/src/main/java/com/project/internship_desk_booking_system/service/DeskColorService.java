package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.dto.DColorDTO;
import com.project.internship_desk_booking_system.entity.DeskColor;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.repository.DeskColorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service layer responsible for managing desk color metadata for ADMIN users.
 *
 * Provides CRUD operations for desk colors, including:
 * - Retrieving all defined colors
 * - Fetching a color by its identifier
 * - Creating new desk color definitions
 * - Updating existing desk color attributes
 * - Deleting color entries
 *
 * Additionally, this service ensures:
 * - Validation of color fields such as name, meaning, and HEX code format
 * - Uniqueness constraints for color name, color code, and meaning
 * - Transformation between entity objects and data transfer objects
 *
 * All operations interact with DeskColorRepository and use ExceptionResponse
 * to enforce domain rules and improve API clarity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeskColorService {

    private final DeskColorRepository deskColorRepository;
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    /**
     * Retrieves all desk colors from the database and maps them to DColorDTOs.
     *
     * @return a list of all desk colors as DColorDTO objects
     */
    @Transactional(readOnly = true)
    public List<DColorDTO> getAllColors() {
        log.info("Fetching all desk colors");
        return deskColorRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a desk color by its ID.
     *
     * @param id the ID of the desk color to fetch
     * @return the desk color as a DColorDTO
     * @throws ExceptionResponse if the color with the given ID is not found
     */
    @Transactional(readOnly = true)
    public DColorDTO getColorById(Long id) {
        log.info("Fetching desk color with id: {}", id);
        DeskColor color = deskColorRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "COLOR_NOT_FOUND",
                        "Desk color not found with id: " + id
                ));
        return toDto(color);
    }

    /**
     * Creates a new desk color entry in the system.
     * Performs validation and uniqueness checks on the color name, code, and meaning.
     *
     * @param dto the DColorDTO containing the color details
     * @return the newly created desk color as a DColorDTO
     * @throws ExceptionResponse if validation fails or uniqueness constraints are violated
     */
    @Transactional
    public DColorDTO createColor(DColorDTO dto) {
        log.info("Creating new desk color: {}", dto.getColorName());

        validateColorDto(dto);

        if (deskColorRepository.existsByColorName(dto.getColorName().trim().toUpperCase())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_NAME_EXISTS",
                    "A color with name '" + dto.getColorName() + "' already exists"
            );
        }

        if (deskColorRepository.existsByColorCode(dto.getColorCode().trim().toUpperCase())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_CODE_EXISTS",
                    "Color code '" + dto.getColorCode() + "' is already in use"
            );
        }

        if (deskColorRepository.existsByColorMeaning(dto.getColorMeaning().trim())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_MEANING_EXISTS",
                    "Color meaning '" + dto.getColorMeaning() + "' already exists"
            );
        }

        DeskColor color = DeskColor.builder()
                .colorName(dto.getColorName().trim().toUpperCase())
                .colorCode(dto.getColorCode().trim().toUpperCase())
                .colorMeaning(dto.getColorMeaning().trim())
                .build();

        DeskColor saved = deskColorRepository.save(color);
        log.info("Desk color created successfully with id: {}", saved.getId());

        return toDto(saved);
    }

    /**
     * Updates an existing desk color by ID.
     * Performs validation and uniqueness checks on updated fields.
     *
     * @param id  the ID of the desk color to update
     * @param dto the updated desk color data
     * @return the updated desk color as a DColorDTO
     * @throws ExceptionResponse if the color is not found or validation/uniqueness checks fail
     */
    @Transactional
    public DColorDTO updateColor(Long id, DColorDTO dto) {
        log.info("Updating desk color with id: {}", id);

        DeskColor existing = deskColorRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "COLOR_NOT_FOUND",
                        "Desk color not found with id: " + id
                ));

        validateColorDto(dto);

        String newName = dto.getColorName().trim().toUpperCase();
        String newCode = dto.getColorCode().trim().toUpperCase();
        String newMeaning = dto.getColorMeaning().trim();

        if (!existing.getColorName().equals(newName)
                && deskColorRepository.existsByColorName(newName)) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_NAME_EXISTS",
                    "A color with name '" + dto.getColorName() + "' already exists"
            );
        }

        if (!existing.getColorCode().equals(newCode)
                && deskColorRepository.existsByColorCode(newCode)) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_CODE_EXISTS",
                    "Color code '" + dto.getColorCode() + "' is already in use"
            );
        }

        if (!existing.getColorMeaning().equals(newMeaning)
                && deskColorRepository.existsByColorMeaning(newMeaning)) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_MEANING_EXISTS",
                    "Color meaning '" + dto.getColorMeaning() + "' already exists"
            );
        }

        existing.setColorName(newName);
        existing.setColorCode(newCode);
        existing.setColorMeaning(newMeaning);

        DeskColor updated = deskColorRepository.save(existing);
        log.info("Desk color updated successfully: {}", updated.getId());

        return toDto(updated);
    }

    /**
     * Deletes a desk color by its ID.
     *
     * @param id the ID of the desk color to delete
     * @throws ExceptionResponse if the color does not exist
     */
    @Transactional
    public void deleteColor(Long id) {
        log.info("Deleting desk color with id: {}", id);

        if (!deskColorRepository.existsById(id)) {
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "COLOR_NOT_FOUND",
                    "Desk color not found with id: " + id
            );
        }

        deskColorRepository.deleteById(id);
        log.info("Desk color deleted successfully: {}", id);
    }

    /**
     * Validates a DColorDTO object.
     * Checks for null/empty fields, correct HEX code format, and length constraints.
     *
     * @param dto the DColorDTO to validate
     * @throws ExceptionResponse if validation fails
     */
    private void validateColorDto(DColorDTO dto) {
        if (dto.getColorName() == null || dto.getColorName().trim().isEmpty()) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_COLOR_NAME",
                    "Color name is required and cannot be empty"
            );
        }

        if (dto.getColorCode() == null || !HEX_COLOR_PATTERN.matcher(dto.getColorCode()).matches()) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_COLOR_CODE",
                    "Color code must be in hex format (#RRGGBB), e.g., #FF5733"
            );
        }

        if (dto.getColorMeaning() == null || dto.getColorMeaning().trim().isEmpty()) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_COLOR_MEANING",
                    "Color meaning is required and cannot be empty"
            );
        }

        if (dto.getColorName().length() > 50) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_NAME_TOO_LONG",
                    "Color name cannot exceed 50 characters"
            );
        }

        if (dto.getColorMeaning().length() > 255) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_MEANING_TOO_LONG",
                    "Color meaning cannot exceed 255 characters"
            );
        }
    }

    /**
     * Maps a DeskColor entity to a DColorDTO.
     *
     * @param entity the DeskColor entity
     * @return the corresponding DColorDTO
     */
    private DColorDTO toDto(DeskColor entity) {
        return DColorDTO.builder()
                .id(entity.getId())
                .colorName(entity.getColorName())
                .colorCode(entity.getColorCode())
                .colorMeaning(entity.getColorMeaning())
                .build();
    }
}
