package com.project.internship_desk_booking_system.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

public interface EmailService {

    void sendBookingConfirmationEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);

    void sendCancelledBookingEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);


    void sendImportantDeskRelatedEmail(String toEmail, String deskName, List<String> changes);

    void sendCancelBookingAdminEmail(String message, String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime
    );
    void sendBookingConfirmationAdminEmail(String message, String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime
    );

}
