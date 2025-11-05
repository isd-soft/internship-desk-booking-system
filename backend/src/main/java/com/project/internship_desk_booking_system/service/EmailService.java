    package com.project.internship_desk_booking_system.service;

    import java.time.OffsetDateTime;

    public interface EmailService {

        void sendBookingConfirmationEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);

        void sendCancelledBookingEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);

        void sendImportantDeskRelatedEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);


    }
