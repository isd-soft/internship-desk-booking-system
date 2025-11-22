package com.project.internship_desk_booking_system.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

public interface EmailService {
    /**
     * Sends a booking confirmation email to the user.
     * @param toEmail recipient's email
     * @param bookingId booking ID
     * @param deskName desk name
     * @param zone zone abbreviation
     * @param dateTime booking date and time
     */
    void sendBookingConfirmationEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);

    /**
     * Sends a booking cancellation email to the user.
     * @param toEmail recipient's email
     * @param bookingId booking ID
     * @param deskName desk name
     * @param zone zone abbreviation
     * @param dateTime cancellation date and time
     */
    void sendCancelledBookingEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);

    /**
     * Sends an important desk-related email to the user about changes.
     * @param toEmail recipient's email
     * @param deskName desk name
     * @param changes list of changes
     */
    void sendImportantDeskRelatedEmail(String toEmail, String deskName, List<String> changes);

    /**
     * Sends a booking cancellation email from admin to the user.
     * @param message admin message
     * @param toEmail recipient's email
     * @param bookingId booking ID
     * @param deskName desk name
     * @param zone zone abbreviation
     * @param dateTime cancellation date and time
     */
    void sendCancelBookingAdminEmail(String message, String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);

    /**
     * Sends a booking confirmation email from admin to the user.
     * @param message admin message
     * @param toEmail recipient's email
     * @param bookingId booking ID
     * @param deskName desk name
     * @param zone zone abbreviation
     * @param dateTime booking date and time
     */
    void sendBookingConfirmationAdminEmail(String message, String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime);
}
