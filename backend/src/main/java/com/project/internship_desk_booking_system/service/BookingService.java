package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponse;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DeskRepository deskRepository;

//    private static final int MaxBookingDaysInAdvance = 30;
//    private static final int MaxBookingHoursPerWeek = 20;
    private static final int MinBookingHours = 1;
    private static final int MaxBookingHours = 8;

    private final BookingMapper bookingMapper;
    private final EmailService emailService;


    public BookingResponseDto createBooking(String email, BookingCreateRequest request){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user with email " +  email));
        Desk desk = deskRepository.findById(request.getDeskId()).orElseThrow(() ->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_DESKID_FOUND", "Cannot find desk id"));

        validateBookingTimes(request.getStartTime(), request.getEndTime());
        checkDeskAvailability(request.getDeskId(), request.getStartTime(), request.getEndTime());
        checkUserAvailability(user.getId(), request.getStartTime(), request.getEndTime());
        //validateWeeklyAvailability(user_id, request.getStartTime(), request.getEndTime());

        Booking booking = Booking.builder()
                .user(user)
                .desk(desk)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        return maptoDto(savedBooking);
    }

    public void cancelBooking(String email, Long id){
        Booking booking = bookingRepository.findById(id).orElseThrow(()->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_EMAIL_FOUND", "Cannot find booking id"));
        if(!booking.getUser().getEmail().equals(email)){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_CANCEL_BOOKING", "Cannot find user email");
        }
        if(!booking.getStartTime().isBefore(LocalDateTime.now())){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before today's date and time");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public void deleteBooking(Long id){
        bookingRepository.deleteById(id);
    }

    public void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime){
        LocalDateTime now = LocalDateTime.now();

        if(startTime.isBefore(now)){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before today's date and time");
        }
        if(endTime.isBefore(startTime)){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "End time must be after start time");
        }
        long hours = Duration.between(startTime,endTime).toHours();

        if(hours > MaxBookingHours){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking more than 8 Hours");
        }
        if(hours < MinBookingHours){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking less than 1 Hour");
        }
    }

    public void checkDeskAvailability(Long desk_id, LocalDateTime startTime, LocalDateTime endTime){
        List<Booking> overLappingBookings = bookingRepository.findOverlappingBookings(
                desk_id,
                startTime,
                endTime
        );

        if(!overLappingBookings.isEmpty()){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "DESK_NOT_AVAILABLE", "Desk is not available for booking due to overlapping time");
        }
    }
    public void checkUserAvailability(Long user_id, LocalDateTime startTime, LocalDateTime endTime){
        List<Booking> userBookings = bookingRepository.findUserBookings(
                user_id,
                startTime,
                endTime);
        if(!userBookings.isEmpty()){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_NOT_AVAILABLE", "Already have a booking in this time period");
        }
    }


    public List<BookingResponse> getAllBookings(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        "USER_NOT_FOUND",
                        "Cannot find user"
                ));

        return user.getBookings()
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }


    public List<BookingResponseDto> getUpcomingBookings(String email){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(()->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user"));
        List<Booking> bookings = bookingRepository.findUpcomingBookingsByUserId(
                user.getId(),
                LocalDateTime.now()
        );
        return bookings.stream().map(this::maptoDto).collect(Collectors.toList());
    }

    public BookingResponseDto getBookingById(String email, Long booking_id){
        Booking booking = bookingRepository.findById(booking_id).orElseThrow(()->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_BOOKING_FOUND", "Cannot find booking"));
        if(!booking.getUser().getEmail().equals(email)){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_NOT_AVAILABLE", "Booking is not available for user with email " +  email);
        }
        return maptoDto(booking);
    }


    private BookingResponseDto maptoDto(Booking booking){
        double durationHours = Duration.between(booking.getStartTime(),booking.getEndTime()).toHours();

        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .deskId(booking.getDesk().getId())
                .deskName(booking.getDesk().getDeskName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().name())
                .durationHours(durationHours)
                .build();
    }


    //optional
/*    private void validateWeeklyAvailability(Long user_id, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime weekStart = sttartTime.toLocalDate().atStartOfDay().with(java.time.DayOfWeek.MONDAY);

        LocalDateTime weekEnd = weekStart.plusDays(7);
        List<Booking> weeklyBookings = bookingRepository.findUserBookings(user_id, weekStart, weekEnd);
        long totalHours = weeklyBookings.stream().
                mapToLong(b->Duration.between(b.getStartTime(),b.getEndTime()).toHours()).sum();
        long todaysHours = Duration.between(startTime,endTime).toHours();
        if(totalHours + todaysHours > MaxBookingHoursPerWeek){
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Already reached maximum amount for this month");
        }
    }*/
    //optional fr-26
/*    private void validateAdvanceBookingLimit(LocalDateTime startTime) {
        LocalDateTime maxAllowedDate = LocalDateTime.now().plusDays(MaxBookingDaysInAdvance);

        if (startTime.isAfter(maxAllowedDate)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot create booking for more than 30 days in advance");
        }
    }*/

}
