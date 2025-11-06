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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("Creating booking for user: {} for desk: {}", email, request.getDeskId());

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user with email " +  email);
        });

        Desk desk = deskRepository.findById(request.getDeskId()).orElseThrow(() -> {
            log.error("Desk not found with id: {}", request.getDeskId());
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_DESKID_FOUND", "Cannot find desk id");
        });
    public BookingResponseDto createBooking(String email, BookingCreateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user with email " + email));
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
        emailService.sendBookingConfirmationEmail(email, booking.getId(), booking.getDesk().getDeskName(), booking.getDesk().getZone(), OffsetDateTime.now());

        log.info("Booking created successfully with id: {} for user: {}", savedBooking.getId(), email);
        return maptoDto(savedBooking);
    }

    public void cancelBooking(String email, Long id){
        log.info("Attempting to cancel booking id: {} for user: {}", id, email);

        Booking booking = bookingRepository.findById(id).orElseThrow(()-> {
            log.error("Booking not found with id: {}", id);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_EMAIL_FOUND", "Cannot find booking id");
        });

        if(!booking.getUser().getEmail().equals(email)){
            log.error("User {} attempted to cancel booking {} belonging to another user", email, id);
    public void cancelBooking(String email, Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_EMAIL_FOUND", "Cannot find booking id"));
        if (!booking.getUser().getEmail().equals(email)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_CANCEL_BOOKING", "Cannot find user email");
        }

        if(!booking.getStartTime().isBefore(LocalDateTime.now())){
            log.error("Cannot cancel booking id: {} as it has already started", id);
        if (!booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before today's date and time");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking id: {} cancelled successfully by user: {}", id, email);
        emailService.sendCancelledBookingEmail(email, booking.getId(), booking.getDesk().getDeskName(), booking.getDesk().getZone(), OffsetDateTime.now());
    }

    public void deleteBooking(Long id){
        log.info("Deleting booking with id: {}", id);
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
        log.info("Booking id: {} deleted successfully", id);
    }

    public void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime){
        log.debug("Validating booking times - Start: {}, End: {}", startTime, endTime);
    public void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if(startTime.isBefore(now)){
            log.error("Invalid booking time: start time {} is before current time {}", startTime, now);
        if (startTime.isBefore(now)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking before today's date and time");
        }

        if(endTime.isBefore(startTime)){
            log.error("Invalid booking time: end time {} is before start time {}", endTime, startTime);
        if (endTime.isBefore(startTime)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "End time must be after start time");
        }

        long hours = Duration.between(startTime,endTime).toHours();
        long hours = Duration.between(startTime, endTime).toHours();

        if(hours > MaxBookingHours){
            log.error("Booking duration {} hours exceeds maximum of {} hours", hours, MaxBookingHours);
        if (hours > MaxBookingHours) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking more than 8 Hours");
        }

        if(hours < MinBookingHours){
            log.error("Booking duration {} hours is less than minimum of {} hour", hours, MinBookingHours);
        if (hours < MinBookingHours) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "WRONG_TIME_DATE", "Cannot start booking less than 1 Hour");
        }

        log.debug("Booking times validated successfully. Duration: {} hours", hours);
    }

    public void checkDeskAvailability(Long desk_id, LocalDateTime startTime, LocalDateTime endTime){
        log.debug("Checking desk availability for desk id: {} between {} and {}", desk_id, startTime, endTime);

    public void checkDeskAvailability(Long desk_id, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> overLappingBookings = bookingRepository.findOverlappingBookings(
                desk_id,
                startTime,
                endTime
        );

        if(!overLappingBookings.isEmpty()){
            log.error("Desk id: {} has {} overlapping bookings for the requested time period", desk_id, overLappingBookings.size());
        if (!overLappingBookings.isEmpty()) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "DESK_NOT_AVAILABLE", "Desk is not available for booking due to overlapping time");
        }

        log.debug("Desk id: {} is available for booking", desk_id);
    }

    public void checkUserAvailability(Long user_id, LocalDateTime startTime, LocalDateTime endTime){
        log.debug("Checking user availability for user id: {} between {} and {}", user_id, startTime, endTime);


    public void checkUserAvailability(Long user_id, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> userBookings = bookingRepository.findUserBookings(
                user_id,
                startTime,
                endTime);

        if(!userBookings.isEmpty()){
            log.error("User id: {} already has {} booking(s) in the requested time period", user_id, userBookings.size());
        if (!userBookings.isEmpty()) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "USER_NOT_AVAILABLE", "Already have a booking in this time period");
        }

        log.debug("User id: {} is available for booking", user_id);
    }


    public List<BookingResponseDto> getUserBookings(String email){
        log.info("Fetching all bookings for user: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(()-> {
            log.error("User not found with email: {}", email);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_EMAIL_FOUND", "Cannot find user with email " +  email);
        });

        List<Booking> bookings = bookingRepository.findUserBookings(
                user.getId(),
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now().plusYears(1)
        );

        log.info("Found {} bookings for user: {}", bookings.size(), email);
        return bookings.stream().map(this::maptoDto).collect(Collectors.toList());
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
        log.info("Fetching upcoming bookings for user: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(()-> {
            log.error("User not found with email: {}", email);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user");
        });

    public List<BookingResponseDto> getUpcomingBookings(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_USERID_FOUND", "Cannot find user"));
        List<Booking> bookings = bookingRepository.findUpcomingBookingsByUserId(
                user.getId(),
                LocalDateTime.now()
        );

        log.info("Found {} upcoming bookings for user: {}", bookings.size(), email);
        return bookings.stream().map(this::maptoDto).collect(Collectors.toList());
    }

    public BookingResponseDto getBookingById(String email, Long booking_id){
        log.info("Fetching booking id: {} for user: {}", booking_id, email);

        Booking booking = bookingRepository.findById(booking_id).orElseThrow(()-> {
            log.error("Booking not found with id: {}", booking_id);
            return new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_BOOKING_FOUND", "Cannot find booking");
        });

        if(!booking.getUser().getEmail().equals(email)){
            log.error("User {} attempted to access booking {} belonging to another user", email, booking_id);
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_NOT_AVAILABLE", "Booking is not available for user with email " +  email);
    public BookingResponseDto getBookingById(String email, Long booking_id) {
        Booking booking = bookingRepository.findById(booking_id).orElseThrow(() ->
                new ExceptionResponse(HttpStatus.BAD_REQUEST, "NO_BOOKING_FOUND", "Cannot find booking"));
        if (!booking.getUser().getEmail().equals(email)) {
            throw new ExceptionResponse(HttpStatus.BAD_REQUEST, "BOOKING_NOT_AVAILABLE", "Booking is not available for user with email " + email);
        }

        log.info("Booking id: {} retrieved successfully for user: {}", booking_id, email);
        return maptoDto(booking);
    }


    private BookingResponseDto maptoDto(Booking booking) {
        double durationHours = Duration.between(booking.getStartTime(), booking.getEndTime()).toHours();

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
