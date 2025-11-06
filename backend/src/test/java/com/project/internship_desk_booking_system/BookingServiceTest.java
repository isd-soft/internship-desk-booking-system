package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.command.BookingCreateRequest;
import com.project.internship_desk_booking_system.command.BookingResponseDto;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.mapper.BookingMapper;
import com.project.internship_desk_booking_system.repository.BookingRepository;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.BookingService;
import com.project.internship_desk_booking_system.service.EmailService;
import com.project.internship_desk_booking_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private EmailService     emailService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Desk desk;
    private Booking booking;
    private BookingCreateRequest bookingCreateRequest;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    public void setUp() {
        //User user = User.builder
    }
}
