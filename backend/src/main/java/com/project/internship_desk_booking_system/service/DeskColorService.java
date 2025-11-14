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

@Slf4j
@Service
@RequiredArgsConstructor
public class DeskColorService {

    private final DeskColorRepository deskColorRepository;
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Transactional(readOnly = true)
    public List<DColorDTO> getAllColors() {
        log.info("Fetching all desk colors");
        return deskColorRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public DColorDTO createColor(DColorDTO dto) {
        log.info("Creating new desk color: {}", dto.getColorName());

        validateColorDto(dto);

        if (deskColorRepository.existsByColorName(dto.getColorName())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_NAME_EXISTS",
                    "A color with name '" + dto.getColorName() + "' already exists"
            );
        }

        if (deskColorRepository.existsByColorCode(dto.getColorCode())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_CODE_EXISTS",
                    "Color code '" + dto.getColorCode() + "' is already in use"
            );
        }
        DeskColor color = DeskColor.builder()
                .colorName(dto.getColorName().toUpperCase())
                .colorCode(dto.getColorCode().toUpperCase())
                .colorMeaning(dto.getColorMeaning())
                .build();

        DeskColor saved = deskColorRepository.save(color);
        log.info("Desk color created successfully with id: {}", saved.getId());

        return toDto(saved);
    }

    @Transactional
    public DColorDTO updateColor(Long id, DColorDTO dto) {
        log.info("Updating desk color with id: {}", id);

        DeskColor color = deskColorRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "COLOR_NOT_FOUND",
                        "Desk color not found with id: " + id
                ));

        validateColorDto(dto);

        if (!color.getColorName().equals(dto.getColorName()) &&
                deskColorRepository.existsByColorName(dto.getColorName())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_NAME_EXISTS",
                    "A color with name '" + dto.getColorName() + "' already exists"
            );
        }

        if (!color.getColorCode().equals(dto.getColorCode()) &&
                deskColorRepository.existsByColorCode(dto.getColorCode())) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "COLOR_CODE_EXISTS",
                    "Color code '" + dto.getColorCode() + "' is already in use"
            );
        }

        color.setColorName(dto.getColorName().toUpperCase());
        color.setColorCode(dto.getColorCode().toUpperCase());
        color.setColorMeaning(dto.getColorMeaning());

        DeskColor updated = deskColorRepository.save(color);
        log.info("Desk color updated successfully: {}", updated.getId());

        return toDto(updated);
    }

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

    private DColorDTO toDto(DeskColor entity) {
        return DColorDTO.builder()
                .id(entity.getId())
                .colorName(entity.getColorName())
                .colorCode(entity.getColorCode())
                .colorMeaning(entity.getColorMeaning())
                .build();
    }
}