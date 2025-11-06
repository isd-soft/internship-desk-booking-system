package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingUpdateCommand;
import com.project.internship_desk_booking_system.dto.DeskDTO;
import com.project.internship_desk_booking_system.dto.DeskUpdateDTO;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;

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
        return toDeskDTO(desk);
    }
    //TODO Prevent deactivation of desks that are currently occupied
    @Transactional
    public DeskDTO deactivateDesk(
            Long id
    ) {
        Desk desk = deskRepository.findById(id)
                .orElseThrow(() -> new ExceptionResponse(HttpStatus.NOT_FOUND, "DESK_NOT_FOUND", "Desk with id: " + id + " not found"));

        desk.setIsTemporarilyAvailable(false);
        desk.setStatus(DeskStatus.DEACTIVATED);

        deskRepository.save(desk);
        return toDeskDTO(desk);
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

        return toDeskDTO(desk);
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
            DeskDTO deskDTO = toDeskDTO(desk);
            deskDTOList.add(deskDTO);
        }
        return deskDTOList;
    }

   @Transactional
    public BookingResponse cancelBooking(
        Long bookingId
    ){
        Booking booking = findBookingById(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse editBooking(
        Long bookingId,
        BookingUpdateCommand bookingUpdateCommand
    ){
        Booking existingBooking = findBookingById(bookingId);

        if(bookingUpdateCommand.userId() != null){
            changeUser(
                    bookingUpdateCommand.userId(),
                    existingBooking);
        }
        if(bookingUpdateCommand.deskId() != null){
            changeDesk(
                    bookingUpdateCommand.deskId(),
                    existingBooking);
        }
        if(bookingUpdateCommand.startTime() != null){
            existingBooking.setStartTime(bookingUpdateCommand.startTime());
        }
        if(bookingUpdateCommand.endTime() != null){
            existingBooking.setEndTime(bookingUpdateCommand.endTime());
        }
        if(bookingUpdateCommand.status() != null){
            existingBooking.setStatus(bookingUpdateCommand.status());
        }

        return  bookingMapper.toResponse(existingBooking);
    }

    private DeskDTO toDeskDTO(
            Desk desk
    ){
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

    private Booking findBookingById(
            Long bookingId
    ){
        return bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND",
                        String.format(
                                "Booking with id:{%d} not found",
                                bookingId
                        )
                ));
    }

    private void changeUser(
            Long userId,
            Booking booking
    ){
        User user = userRepository
                .findById(userId)
                .orElseThrow(()-> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        String.format(
                                "User with id: {%d} not found ",
                                userId)
                ));
        booking.setUser(user);
    }

    private void changeDesk(
            Long deskId,
            Booking booking
    ){
        Desk desk = deskRepository
                .findById(deskId)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        "DESK_NOT_FOUND",
                        String.format(
                                "Desk with id: {%d} not found",
                                deskId
                        )
                ));
        booking.setDesk(desk);
    }
}
