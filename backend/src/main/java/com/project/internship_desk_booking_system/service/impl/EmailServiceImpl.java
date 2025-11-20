package com.project.internship_desk_booking_system.service.impl;

import com.project.internship_desk_booking_system.error.ExceptionResponse;
import com.project.internship_desk_booking_system.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom("noreply@isd-booking.com", "ISD Booking System");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mime);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new ExceptionResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "EMAIL_SENDING_FAILED",
                    "Failed to send email",
                    Map.of(
                            "recipient", to,
                            "subject", subject,
                            "error", e.getMessage()
                    )
            );
        }
    }

    @Override
    @Async
    public void sendBookingConfirmationEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime) {
        String formattedDate = dateTime.toLocalDate().toString();
        String formattedTime = dateTime.toLocalTime().withSecond(0).withNano(0).toString();

        String subject = "Booking Confirmation – ISD";
        String html = """
                <p>Hello,</p>
                <p>Your booking has been <strong>confirmed</strong>!</p>
                <p><strong>Details:</strong></p>
                <ul>
                    <li><strong>Booking ID:</strong> %s</li>
                    <li><strong>Desk:</strong> %s</li>
                    <li><strong>Zone:</strong> %s</li>
                    <li><strong>Date:</strong> %s</li>
                    <li><strong>Time:</strong> %s</li>
                </ul>
                <p>If you have any questions, please contact our support team.</p>
                """.formatted(bookingId, deskName, zone, formattedDate, formattedTime);

        sendHtml(toEmail, subject, wrap(html));

    }

    @Override
    @Async
    public void sendCancelledBookingEmail(String toEmail, Long bookingId, String deskName, String zone, OffsetDateTime dateTime) {
        String formattedDate = dateTime.toLocalDate().toString();
        String formattedTime = dateTime.toLocalTime().withSecond(0).withNano(0).toString();

        String subject = "Booking Cancelled – ISD";
        String html = """
                <p>Hello,</p>
                <p>Your booking has been <strong>cancelled</strong>.</p>
                <p><strong>Details:</strong></p>
                <ul>
                    <li><strong>Desk:</strong> %s</li>
                    <li><strong>Zone:</strong> %s</li>
                    <li><strong>Date:</strong> %s</li>
                    <li><strong>Time:</strong> %s</li>
                </ul>
                <p>If this was a mistake, please make a new booking in the system.</p>
                """.formatted(deskName, zone, formattedDate, formattedTime);

        sendHtml(toEmail, subject, wrap(html));
    }


    @Override
    @Async
    public void sendImportantDeskRelatedEmail(String toEmail, String deskName, List<String> changes) {
        String subject = "Desk Updated – ISD";

        String changesHtml = changes.stream()
                .map(change -> "<li>" + change + "</li>")
                .collect(Collectors.joining());

        String html = """
            <p>Hello,</p>
            <p>The desk <strong>%s</strong> has been updated.</p>
            
            <p><strong>Changes:</strong></p>
            <ul style="padding-left:16px;">
                %s
            </ul>
            
            <p>Please check the ISD Booking System for more details.</p>
            """.formatted(deskName, changesHtml);

        sendHtml(toEmail, subject, wrap(html));

    }

    @Override
    @Async
    public void sendBookingConfirmationAdminEmail(
            String message,
            String toEmail,
            Long bookingId,
            String deskName,
            String zone,
            OffsetDateTime dateTime
    ) {

        String formattedDate = dateTime.toLocalDate().toString();
        String formattedTime = dateTime.toLocalTime().withSecond(0).withNano(0).toString();

        String subject = "Admin Created a Booking for You – ISD";

        String html = """
                <p>Hello,</p>
                <p>An administrator has <strong>created a booking</strong> for you.</p>
                
                <p><strong>Message from administrator:</strong></p>
                <blockquote style="border-left:4px solid #ff8a00;padding-left:12px;color:#555;">
                  %s
                </blockquote>
                
                <p><strong>Booking Details:</strong></p>
                <ul>
                    <li><strong>Booking ID:</strong> %s</li>
                    <li><strong>Desk:</strong> %s</li>
                    <li><strong>Zone:</strong> %s</li>
                    <li><strong>Date:</strong> %s</li>
                    <li><strong>Time:</strong> %s</li>
                </ul>
                
                <p>You can view more details in the ISD Booking System.</p>
                """.formatted(message, bookingId, deskName, zone, formattedDate, formattedTime);

        sendHtml(toEmail, subject, wrap(html));
    }

    @Override
    @Async
    public void sendCancelBookingAdminEmail(
            String message,
            String toEmail,
            Long bookingId,
            String deskName,
            String zone,
            OffsetDateTime dateTime
    ) {

        String formattedDate = dateTime.toLocalDate().toString();
        String formattedTime = dateTime.toLocalTime().withSecond(0).withNano(0).toString();

        String subject = "Admin Cancelled Your Booking – ISD";

        String html = """
                <p>Hello,</p>
                <p>An administrator has <strong>cancelled your booking</strong>.</p>
                
                <p><strong>Message from administrator:</strong></p>
                <blockquote style="border-left:4px solid #ff8a00;padding-left:12px;color:#555;">
                  %s
                </blockquote>
                
                <p><strong>Booking Details:</strong></p>
                <ul>
                    <li><strong>Booking ID:</strong> %s</li>
                    <li><strong>Desk:</strong> %s</li>
                    <li><strong>Zone:</strong> %s</li>
                    <li><strong>Date:</strong> %s</li>
                    <li><strong>Time:</strong> %s</li>
                </ul>
                
                <p>If this was unexpected, please contact your administrator.</p>
                """.formatted(message, bookingId, deskName, zone, formattedDate, formattedTime);

        sendHtml(toEmail, subject, wrap(html));
    }


    private String wrap(String innerHtml) {
        String template = """
                <div style='font-family:Segoe UI,Roboto,Arial,sans-serif;
                            font-size:14px;line-height:1.6;color:#111;
                            background-color:#f9f9f9;padding:24px 0'>
                  <div style='max-width:580px;margin:0 auto;
                              background:#fff;
                              border:1px solid #eee;
                              border-radius:8px;
                              box-shadow:0 2px 6px rgba(0,0,0,0.05);
                              overflow:hidden;'>
                
                    <!-- HEADER -->
                    <div style='background:linear-gradient(135deg, #ff8a00, #ffb347);
                                text-align:center;
                                padding:28px 0;'>
                      <div style='color:#fff;
                                  font-weight:700;
                                  font-size:22px;
                                  letter-spacing:0.5px;'>
                        ISD Booking System
                      </div>
                    </div>
                
                    <!-- BODY -->
                    <div style='padding:28px 24px;'>
                      %s
                    </div>
                
                    <!-- FOOTER -->
                    <div style='font-size:12px;color:#888;
                                text-align:center;border-top:1px solid #eee;
                                padding:16px 0;background-color:#fafafa;'>
                      This is an automated message. Please do not reply to this email.
                    </div>
                  </div>
                </div>
                """;
        return String.format(template, innerHtml);
    }
}
