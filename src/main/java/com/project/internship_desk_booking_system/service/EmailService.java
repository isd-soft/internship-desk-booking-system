    package com.project.internship_desk_booking_system.service;

    import java.time.OffsetDateTime;

    public interface EmailService {

        void sendBookingConfirmationEmail(String toEmail, Long bookingId, String location, OffsetDateTime dateTime);

        void sendCancelledBookingEmail(String email, String location, OffsetDateTime dateTime);

        void sendImportantDeskRelatedEmail(String email, String location, OffsetDateTime dateTime);


    }
