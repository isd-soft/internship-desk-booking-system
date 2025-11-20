package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.command.CoordinatesUpdateCommand;
import com.project.internship_desk_booking_system.dto.*;
import com.project.internship_desk_booking_system.entity.*;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.mapper.DeskMapper;
import com.project.internship_desk_booking_system.mapper.ImageMapper;
import com.project.internship_desk_booking_system.mapper.ZoneMapper;
import com.project.internship_desk_booking_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service used by administrators to manage desks, zones, users and bookings.
 * <p>
 * Provides operations such as:
 * <ul>
 *   <li>CRUD operations for desks (including soft delete & restore)</li>
 *   <li>Updating desk coordinates and availability windows</li>
 *   <li>Editing and cancelling bookings</li>
 *   <li>User management (role changes, deleting users)</li>
 *   <li>Zone retrieval and mapping</li>
 * </ul>
 * This service contains business rules and validations related to desk booking workflow.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;
    private final ZoneRepository zoneRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final DeskMapper deskMapper;
    private final BookingTimeLimitsService bookingTimeLimitsService;
    private final BookingServiceValidation bookingValidation;
    private final ZoneMapper zoneMapper;
    private final AdminServiceValidation adminServiceValidation;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final EmailService emailService;

    @Value("${app.default-admin-id}")
    private Long defaultAdminId;

    public void deleteUser(Long userId) {
        if (userId.equals(defaultAdminId)) {
            log.warn("Cannot delete default admin");
            throw new ExceptionResponse(HttpStatus.UNAUTHORIZED, "CANNOT_DELETE_DEFAULT_ADMIN", "Cannot delete default admin");
        }
        deskRepository.deleteById(userId);
    }

    private void applyTemporaryAvailability(
            Desk desk,
            Boolean isTemporarilyAvailable,
            LocalDateTime from,
            LocalDateTime until
    ) {
        boolean enabled = Boolean.TRUE.equals(isTemporarilyAvailable);
        desk.setIsTemporarilyAvailable(enabled);
        if (enabled) {
            if (from == null || until == null) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "Start and end date must be provided when temporary availability is enabled"

                );
            }
            if (from.isAfter(until)) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "Start date cannot be after end date");
            }

            if (until.isBefore(LocalDateTime.now())) {
                log.warn("Temporary availability end date {} is in the past", until);
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "End date cannot be in the past");
            }

            desk.setTemporaryAvailableFrom(from);
            desk.setTemporaryAvailableUntil(until);
        } else {
            log.info("Desk {} is no longer temporarily available", desk.getId());
            desk.setTemporaryAvailableFrom(null);
            desk.setTemporaryAvailableUntil(null);
        }
    }

    public DeskDto getDeskById(Long deskId) {
        Desk desk = deskRepository.findById(deskId).orElseThrow(() -> {
            log.warn("Desk with id {} not found", deskId);
            return new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", String.format("Desk with id %d not found", deskId));
        });

        return deskMapper.toDto(desk);
    }

    /**
     * Creates new desk with deskName, Zone, type, status, TemporaryAvailability for 20 days, map coordinates
     *
     * @param deskDto
     * @return new deskDto mapped to deskRepository
     */
    public DeskDto addDesk(
            DeskDto deskDto
    ) {
        log.info(
                "Adding new desk: name={}, zone={}, type={}, status={}",
                deskDto.displayName(),
                deskDto.zoneDto().getId(),
                deskDto.type(),
                deskDto.deskStatus()
        );
        Zone zone = zoneRepository.findById(deskDto.zoneDto().getId())
                .orElseThrow(() -> new RuntimeException("Zone not found: " + deskDto.zoneDto()));
        Desk desk = new Desk();
        desk.setDeskName((deskDto.displayName()));
        desk.setZone(zone);

        desk.setType(deskDto.type() == null ? DeskType.SHARED : deskDto.type());
        desk.setStatus(deskDto.deskStatus() == null ? DeskStatus.ACTIVE : deskDto.deskStatus());
        applyTemporaryAvailability(desk, deskDto.isTemporarilyAvailable(), LocalDateTime.now(), LocalDateTime.now().plusDays(20));
        desk.setCurrentX(deskDto.currentX() == null ? null : deskDto.currentX());
        desk.setCurrentY(deskDto.currentY() == null ? null : deskDto.currentY());
        desk.setBaseX(deskDto.baseX() == null ? null : deskDto.baseX());
        desk.setBaseY(deskDto.baseY() == null ? null : deskDto.baseY());
        desk.setHeight(deskDto.height() == null ? null : deskDto.height());
        desk.setWidth(deskDto.width() == null ? null : deskDto.width());

        deskRepository.save(desk);

        log.info("Desk saved successfully with id {}", desk.getId());

        return deskMapper.toDto(desk);
    }

    /**
     * Deactivates an existing desk and sets it Temporary Unavailable
     *
     * @param id
     * @return updates desk status mapped to DeskDto
     * @throws ExceptionResponse if the deskId was not found in the DeskRepository
     */
    @Transactional
    public DeskDto deactivateDesk(Long id) {
        log.info("Deactivating desk with id {}", id);

        Desk desk = deskRepository.findById(id).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id: " + id + " not found"));

        desk.setIsTemporarilyAvailable(false);
        desk.setStatus(DeskStatus.DEACTIVATED);

        deskRepository.save(desk);

        log.info("Desk {} deactivated successfully", id);

        return deskMapper.toDto(desk);
    }

    /**
     * Activates an existing desk
     *
     * @param id
     * @return updates desk status mapped to DeskDto
     * @throws ExceptionResponse if the deskId was not found in the DeskRepository
     */
    public DeskDto activateDesk(
            Long id
    ) {
        log.info("Activating desk with id {}", id);

        Desk desk = deskRepository.findById(id).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id: " + id + " not found"));
        desk.setStatus(DeskStatus.ACTIVE);
        deskRepository.save(desk);
        log.info("Desk {} activated successfully", id);
        return deskMapper.toDto(desk);
    }

    /**
     * Edites an existing desk
     *
     * @param id, DeskUpdsteDto
     * @return updates desk with what was changed (displayName, Zone, type, status,
     * TemporaryAvailability, map coordinates
     * @throws ExceptionResponse if the deskId was not found in the DeskRepository,
     *                           if the deskName already exists in database, because of the unique contraints,
     *                           if the zoneId was not found in the ZoneRepository
     */
    @Transactional
    public DeskDto editDesk(Long id, DeskUpdateDTO updates) {
        log.info("Editing desk with id {}", id);

        Desk desk = deskRepository.findById(id).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id: " + id + " not found"));

        if (updates.displayName() != null) {
            String newName = normalizeName(updates.displayName());
            adminServiceValidation.validateDeskNameUniqueness(desk.getDeskName(), newName);
            desk.setDeskName(newName);
        }
        if (updates.zoneId() != null) {
            Zone zone = zoneRepository.findById(updates.zoneId()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "ZONE_NOT_FOUND", "Zone not found: " + updates.zoneId()));
            desk.setZone(zone);
        }
        if (updates.type() != null) {
            adminServiceValidation.applyAutoDeactivationForType(desk, updates.type());
        }
        if (updates.deskStatus() != null) {
            desk.setStatus(updates.deskStatus());
        }
        if (updates.isTemporarilyAvailable() != null) {
            applyTemporaryAvailability(desk, updates.isTemporarilyAvailable(), updates.temporaryAvailableFrom(), updates.temporaryAvailableUntil());
        }
        if (updates.currentX() != null) {
            desk.setCurrentX(updates.currentX());
        }
        if (updates.currentY() != null) {
            desk.setCurrentY(updates.currentY());
        }
        if (updates.baseX() != null) {
            desk.setBaseX(updates.baseX());
        }
        if (updates.baseY() != null) {
            desk.setBaseY(updates.baseY());
        }
        if (updates.height() != null) {
            desk.setHeight(updates.height());
        }
        if (updates.width() != null) {
            desk.setWidth(updates.width());
        }

        deskRepository.save(desk);

        log.info("Desk {} updated successfully", id);

        return deskMapper.toDto(desk);
    }


// Update the deleteDesk method in AdminService.java

    /**
     * Soft deletes a desk by marking it as deleted, but still leave it in the DB,
     * so the Admin could restore it.
     * <p>
     * If the desk has active or scheduled bookings, they will be cancelled
     * before completing the deletion.
     *
     * @param id     the ID of the desk
     * @param reason optional reason for deletion
     * @throws ExceptionResponse if the deskId was not found in the DeskRepository
     */
    @Transactional
    public void deleteDesk(Long id, String reason) {
        Desk desk = deskRepository.findById(id).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk not found with id: " + id));

        List<Booking> bookings = new ArrayList<>();

        if (hasActiveBookings(desk)) {
            log.info("Desk {} has active bookings. Cancelling them before deletion.", id);
            bookings.addAll(bookingRepository.findActiveBookingsForDesk(id));
            bookingRepository.cancelAllActiveBookingsForDesk(id);
            log.info("All active bookings for desk {} have been cancelled", id);
        }
        if (hasScheduledBookings(desk)) {
            log.info("Desk {} has scheduled bookings. Cancelling them before deletion.", id);
            bookings.addAll(bookingRepository.findPendingBookingsForDesk(id));
            bookingRepository.cancelAllPendingBookingsForDesk(id);
            log.info("All scheduled bookings for desk {} have been cancelled", id);
        }

        desk.setIsDeleted(true);
        desk.setDeletedAt(LocalDateTime.now());
        desk.setReasonOfDeletion(reason != null && !reason.trim().isEmpty() ? reason.trim() : "No reason provided");

        deskRepository.save(desk);
        log.info("Desk {} soft deleted successfully with reason: {}", id, reason);
        for (Booking booking : bookings) {
            try {
                String userEmail = booking.getUser().getEmail();
                emailService.sendCancelledBookingEmail(
                        userEmail,
                        booking.getId(),
                        booking.getDesk().getDeskName(),
                        booking.getDesk().getZone().getZoneAbv(),
                        OffsetDateTime.now()
                );
                log.info("Cancellation email sent to {} for booking {}", userEmail, booking.getId());
            } catch (Exception e) {
                log.error("Failed to send cancellation email for booking {}: {}",
                        booking.getId(), e.getMessage());
            }
        }

        log.info("Sent {} cancellation emails for deleted desk {}", bookings.size(), id);
    }

    @Transactional
    public void restoreDesk(Long id) {
        Desk desk = deskRepository.findByIdIncludingDeleted(id).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk not found with id: " + id));

        desk.setIsDeleted(false);
        desk.setDeletedAt(null);
        deskRepository.save(desk);

        log.info("Desk {} restored successfully", id);
    }

    @Transactional
    public void restoreCoordinates() {
        deskRepository.restoreCoordinates();
    }

    public List<DeskDto> getAllDesks() {
        List<Desk> desks = deskRepository.findAll();
        List<DeskDto> deskDtoList = new ArrayList<>();
        for (Desk desk : desks) {
            DeskDto deskDTO = deskMapper.toDto(desk);
            deskDtoList.add(deskDTO);
        }
        return deskDtoList;
    }

    @Transactional
    public Integer saveAllDesks(
            List<DeskDto> updates
    ) {
        int count = 0;
        for (DeskDto deskDto : updates) {
            boolean changed = false;

            log.info("Looking for desk with id {}", deskDto.id());
            Desk desk = getDeskOrCreate(deskDto);

            if (!deskDto.displayName().equals(desk.getDeskName())) {
                desk.setDeskName(deskDto.displayName());
                changed = true;
            }
            if (!deskDto.zoneDto().getId().equals(desk.getZone().getId())) {
                Zone zone = zoneRepository.findById(deskDto.zoneDto().getId()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "ZONE_NOT_FOUND", String.format("Zone with id %d not found", deskDto.zoneDto().getId())));

                desk.setZone(zone);
                changed = true;
            }
            if (!deskDto.currentX().equals(desk.getCurrentX())) {
                desk.setCurrentX(deskDto.currentX());
                changed = true;
            }
            if (!deskDto.currentY().equals(desk.getCurrentY())) {
                desk.setCurrentY(deskDto.currentY());
                changed = true;
            }
            if (!deskDto.height().equals(desk.getHeight())) {
                desk.setHeight(deskDto.height());
                changed = true;
            }
            if (!deskDto.width().equals(desk.getWidth())) {
                desk.setWidth(deskDto.width());
                changed = true;
            }

            if (changed || desk.getId() == null) {
                deskRepository.save(desk);
                count++;
            }
        }
        return count;
    }

    private Desk getDeskOrCreate(DeskDto deskDto) {
        return deskRepository.findById(deskDto.id()).orElseGet(() -> {
            log.info("Creating new desk as ID {} was not found", deskDto.id());

            Zone zone = zoneRepository.findById(deskDto.zoneDto().getId()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "ZONE_NOT_FOUND", String.format("Zone with id %d was not found", deskDto.zoneDto().getId())));
            Desk newDesk = new Desk();
            newDesk.setDeskName(deskDto.displayName());
            newDesk.setZone(zone);
            newDesk.setCurrentX(deskDto.currentX());
            newDesk.setCurrentY(deskDto.currentY());
            newDesk.setBaseX(deskDto.currentX());
            newDesk.setBaseY(deskDto.currentY());
            newDesk.setHeight(deskDto.height());
            newDesk.setWidth(deskDto.width());

            return newDesk;
        });
    }

    public List<DeskCoordinatesDTO> getBaseCoordinates() {
        List<DeskCoordinatesDTO> coordinates = deskRepository.findBaseCoordinates();
        if (coordinates.isEmpty()) {
            throw new ExceptionResponse(HttpStatus.NOT_FOUND, "CURRENT_DESK_COORDINATES_NOT_FOUND", "Current coordinates not found ");
        }
        return coordinates;
    }

    @Transactional
    public DeskCoordinatesDTO changeCurrentCoordinates(CoordinatesUpdateCommand coordinatesUpdateCommand) {
        Desk desk = deskRepository.findById(coordinatesUpdateCommand.deskId()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", String.format("Desk with id %d not found ", coordinatesUpdateCommand.deskId())));
        desk.setCurrentX(coordinatesUpdateCommand.currentX());
        desk.setCurrentY(coordinatesUpdateCommand.currentY());

        deskRepository.save(desk);

        return new DeskCoordinatesDTO(desk.getId(), desk.getDeskName(), desk.getCurrentX(), desk.getCurrentY(), desk.getHeight(), desk.getWidth());
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String reason) {
        Booking bookingToCancel = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found"));

        if (bookingToCancel.getStatus() == BookingStatus.CANCELLED) {
            throw new ExceptionResponse(
                    HttpStatus.BAD_REQUEST,
                    "ALREADY_CANCELLED",
                    "This booking is already cancelled"
            );
        }
        bookingToCancel.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(bookingToCancel);

        String userEmail = bookingToCancel.getUser().getEmail();

        try {
            emailService.sendCancelBookingAdminEmail(
                    reason,
                    userEmail,
                    bookingToCancel.getId(),
                    bookingToCancel.getDesk().getDeskName(),
                    bookingToCancel.getDesk().getZone().getZoneName(),
                    OffsetDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to send cancellation email for booking {}: {}",
                    bookingId, e.getMessage(), e);
        }

        return bookingMapper.toResponse(bookingToCancel);
    }

    @Transactional
    public BookingResponse editBooking(Long bookingId, BookingUpdateCommand bookingUpdateCommand) {
        log.info("Looking for a booking with id: {}", bookingId);

        Booking existingBooking = findBookingById(bookingId);

        Long newUserId = bookingUpdateCommand.userId();
        Long currentUserId = existingBooking.getUser().getId();
        Long newDeskId = bookingUpdateCommand.deskId();
        Long currentDeskId = existingBooking.getDesk().getId();
        if (newDeskId != null && !newDeskId.equals(currentDeskId)) {

            Desk newDesk = deskRepository.findById(newDeskId).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id " + bookingUpdateCommand.deskId() + " not found"));


            if (newDesk.getType() == DeskType.ASSIGNED) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "DESK_ASSIGNED", "A assigned desk cant have active bookings");
            }

            bookingValidation.validateTemporaryWindow(newDesk, bookingUpdateCommand.startTime(), bookingUpdateCommand.endTime());

        }


        LocalDateTime finalStartTime = bookingUpdateCommand.startTime() != null ? bookingUpdateCommand.startTime() : existingBooking.getStartTime();

        LocalDateTime finalEndTime = bookingUpdateCommand.endTime() != null ? bookingUpdateCommand.endTime() : existingBooking.getEndTime();

        boolean userIsChanging = newUserId != null && !newUserId.equals(currentUserId);
        if (userIsChanging) {
            List<Booking> userBookings = bookingRepository.findUserBookings(newUserId, finalStartTime, finalEndTime);

            if (!userBookings.isEmpty()) {
                log.error("User id: {} already has {} booking(s) in the requested time period", newUserId, userBookings.size());
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_NOT_AVAILABLE", "User already has a booking in this time period");
            }
        }
        if (bookingUpdateCommand.startTime() != null) {
            if (finalStartTime.isBefore(LocalDateTime.now())) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before current time");
            }

            BookingTimeLimits policy = bookingTimeLimitsService.getActivePolicy();
            long daysInAdvance = ChronoUnit.DAYS.between(LocalDate.now(), finalStartTime.toLocalDate());

            if (daysInAdvance > policy.getMaxDaysInAdvance()) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_TOO_FAR_AHEAD", String.format("Cannot book more than %d days in advance. You are trying to book %d days ahead.", policy.getMaxDaysInAdvance(), daysInAdvance));
            }
        }
        bookingValidation.validateOfficeHours(finalStartTime, finalEndTime);
        bookingValidation.validateBookingTimes(finalStartTime, finalEndTime);
        if (bookingUpdateCommand.endTime() != null) {
            if (finalEndTime.isBefore(finalStartTime)) {
                throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "End time must be after start time");
            }
        }

        if (bookingUpdateCommand.deskId() != null) {
            Desk desk = deskRepository.findById(bookingUpdateCommand.deskId()).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id " + bookingUpdateCommand.deskId() + " not found"));
        }
        if (userIsChanging) {
            log.info("Changing user from {} to {} for booking {}", currentUserId, newUserId, bookingId);
            changeUser(newUserId, existingBooking);
        }

        if (bookingUpdateCommand.deskId() != null) {
            log.info("Editing desk with id {} for booking with id {}", bookingUpdateCommand.deskId(), bookingId);
            changeDesk(bookingUpdateCommand.deskId(), existingBooking);
        }

        existingBooking.setStartTime(finalStartTime);
        existingBooking.setEndTime(finalEndTime);

        if (bookingUpdateCommand.status() != null) {
            log.info("Editing status {} for booking with id {}", bookingUpdateCommand.status(), bookingId);
            existingBooking.setStatus(bookingUpdateCommand.status());
        }

        Booking saved = bookingRepository.save(existingBooking);
        return bookingMapper.toResponse(saved);
    }


    private boolean hasActiveBookings(Desk desk) {
        return bookingRepository.existsActiveBookingsByDeskId(desk.getId(), LocalDateTime.now());
    }

    private boolean hasScheduledBookings(Desk desk) {
        return bookingRepository.existsScheduledBookingsByDeskId(desk.getId(), LocalDateTime.now());
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", String.format("Booking with id:{%d} not found", bookingId)));
    }

    private void changeUser(Long userId, Booking booking) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", String.format("User with id: {%d} not found ", userId)));
        booking.setUser(user);
    }

    private void changeDesk(Long deskId, Booking booking) {
        Desk desk = deskRepository.findById(deskId).orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", String.format("Desk with id: {%d} not found", deskId)));
        booking.setDesk(desk);
    }

    public List<DeskDto> getAllDeletedDesks() {
        List<Desk> deletedDesks = deskRepository.findAllDeleted();
        return deletedDesks.stream().map(deskMapper::toDto).toList();
    }

    public List<EmailRoleDTO> getAllRegisteredUserEmails() {
        log.info("Fetching all unique user emails from bookings");
        List<EmailRoleDTO> users = userRepository.findDistinctEmailAndRole();
        log.info("Found {} unique users", users.size());

        if (users.isEmpty()) {
            log.warn("No users were found");
        }

        return users;
    }

    public List<ZoneDto> getAllZones() {
        List<Zone> zones = zoneRepository.findAll();
        List<ZoneDto> zoneDtoList = new ArrayList<>();
        for (Zone zone : zones) {
            ZoneDto zoneDTO = zoneMapper.toDto(zone);
            zoneDtoList.add(zoneDTO);
        }
        return zoneDtoList;
    }

    public List<ImageDto> getAllImages() {
        List<Image> images = imageRepository.findAll();
        if (images.isEmpty()) {
            throw new ExceptionResponse(
                    HttpStatus.NOT_FOUND,
                    "IMAGES_NOT_FOUND",
                    "Images are not found"
            );
        }
        return images
                .stream()
                .map(imageMapper::toImageDto)
                .toList();
    }

    @Transactional
    public void uploadImage(
            MultipartFile file
    ) {
        Image newImage = new Image();
        newImage.setFileName(file.getOriginalFilename());

        try {
            newImage.setImageData(file.getBytes());
        } catch (Exception e) {
            throw new ExceptionResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CANNOT_READ_IMAGE_FILE",
                    "Can't read image file"
            );
        }
        log.info(
                "id:{} fileName {} ContentType {}",
                newImage.getId(),
                newImage.getFileName(),
                newImage.getContentType()
        );
        newImage.setContentType(file.getContentType());
        imageRepository.save(newImage);
    }

    @Transactional
    public EmailRoleDTO updateUserRole(EmailRoleDTO dto, CustomUserPrincipal principal) {
        log.info("Admin requested to update role for email: {}", dto.getEmail());

        adminServiceValidation.validateNotUpdatingSelf(principal.getEmail(), dto.getEmail());

        User currentUser = userRepository.findByEmailIgnoreCase(principal.getEmail()).orElseThrow(() -> {
            log.error("Current admin user with email {} not found", principal.getEmail());
            return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "CURRENT_USER_NOT_FOUND", "Current admin user not found");
        });

        adminServiceValidation.validateIsAdmin(currentUser);

        User targetUser = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElseThrow(() -> {
            log.warn("User with email {} not found", dto.getEmail());
            return new ExceptionResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found");
        });

        Role oldRole = targetUser.getRole();
        targetUser.setRole(dto.getRole());
        userRepository.save(targetUser);

        log.info("Updated user role: {} -> {}", oldRole, dto.getRole());
        return new EmailRoleDTO(targetUser.getEmail(), targetUser.getRole());
    }


    @Transactional
    public void setBackgroundImage(
            Long id
    ) {
        Image newBackground = imageRepository
                .findById(id)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "IMAGE_NOT_FOUND",
                        String.format(
                                "Image with id %d not found",
                                id
                        )
                ));

        Optional<Image> backgroundImage = imageRepository.findBackground();

        backgroundImage
                .filter(img -> !img.getId().equals(id))
                .ifPresent(img -> img.setBackground(false));

        newBackground.setBackground(true);
    }

    private String normalizeName(String name) {
        if (name == null) return null;

        return name.trim().replaceAll("\\s+", " ");
    }


}