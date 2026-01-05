package com.SmartPlanner.SmartPlanner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:SmartPlanner}")
    private String appName;

    // Send simple text email
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    // Send HTML email
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            log.info("Attempting to send HTML email to: {} with subject: {}", to, subject);
            log.info("Using from email: {}", fromEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("MessagingException while sending HTML email to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending HTML email to {}: {}", to, e.getMessage(), e);
        }
    }

    // Generic Daily Countdown Reminder (works for any day from 10 to 1)
    public void sendDailyCountdownReminder(String to, String cityName, String country,
                                            LocalDate startDate, long daysRemaining) {
        String subject;
        String customMessage;

        if (daysRemaining == 10) {
            subject = "🗓️ " + daysRemaining + " Days Until Your " + cityName + " Trip!";
            customMessage = "Your adventure is just around the corner! Start preparing for your trip.";
        } else if (daysRemaining == 7) {
            subject = "📅 One Week Left! " + cityName + " Trip";
            customMessage = "One week to go! Time to start your preparation checklist.";
        } else if (daysRemaining == 5) {
            subject = "⏳ 5 Days Left! " + cityName + " Trip";
            customMessage = "Almost there! Make sure your travel documents are ready.";
        } else if (daysRemaining == 3) {
            subject = "⏰ Only 3 Days Left! " + cityName + " Trip";
            customMessage = "Time to finalize your packing and travel arrangements!";
        } else if (daysRemaining == 2) {
            subject = "📦 2 Days Left! Pack Your Bags for " + cityName;
            customMessage = "Final countdown! Double-check your packing list and bookings.";
        } else if (daysRemaining == 1) {
            subject = "🎒 Tomorrow is the Day! " + cityName + " Trip";
            customMessage = "Pack your bags, check your documents, and get ready for an amazing journey!";
        } else {
            subject = "🗓️ " + daysRemaining + " Days Until Your " + cityName + " Trip!";
            customMessage = "Your trip countdown continues! " + daysRemaining + " days remaining.";
        }

        String htmlBody = buildReminderEmailHtml(cityName, country, startDate, daysRemaining, customMessage);
        sendHtmlEmail(to, subject, htmlBody);
    }

    // Legacy methods for backward compatibility
    public void sendTripReminder10Days(String to, String cityName, String country,
                                        LocalDate startDate, long daysRemaining) {
        sendDailyCountdownReminder(to, cityName, country, startDate, daysRemaining);
    }

    public void sendTripReminder3Days(String to, String cityName, String country,
                                       LocalDate startDate, long daysRemaining) {
        sendDailyCountdownReminder(to, cityName, country, startDate, daysRemaining);
    }

    public void sendTripReminder1Day(String to, String cityName, String country, LocalDate startDate) {
        sendDailyCountdownReminder(to, cityName, country, startDate, 1);
    }

    // Trip Starts Today
    public void sendTripStartsToday(String to, String cityName, String country, LocalDate startDate) {
        String subject = "🎉 Your " + cityName + " Trip Starts Today!";
        String htmlBody = buildTripStartEmailHtml(cityName, country, startDate);

        sendHtmlEmail(to, subject, htmlBody);
    }

    // Trip Completed
    public void sendTripCompleted(String to, String cityName, String country,
                                   LocalDate startDate, LocalDate endDate) {
        String subject = "✅ Trip Completed - " + cityName + ", " + country;
        String htmlBody = buildTripCompletedEmailHtml(cityName, country, startDate, endDate);

        sendHtmlEmail(to, subject, htmlBody);
    }

    // Trip Cancelled
    public void sendTripCancelled(String to, String cityName, String country) {
        String subject = "❌ Trip Cancelled - " + cityName + ", " + country;
        String htmlBody = buildTripCancelledEmailHtml(cityName, country);

        sendHtmlEmail(to, subject, htmlBody);
    }

    // HTML Email Templates
    private String buildReminderEmailHtml(String cityName, String country,
                                           LocalDate startDate, long daysRemaining, String customMessage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                    .countdown { font-size: 48px; font-weight: bold; margin: 10px 0; }
                    .content { padding: 30px; }
                    .trip-details { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .footer { background: #333; color: white; padding: 20px; text-align: center; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <div class="countdown">%d DAYS</div>
                        <p>until your trip!</p>
                    </div>
                    <div class="content">
                        <p>Hello Traveler! 👋</p>
                        <p>%s</p>
                        <div class="trip-details">
                            <h3>📍 Destination: %s, %s</h3>
                            <p>📅 Start Date: %s</p>
                        </div>
                        <p>Have a wonderful journey!</p>
                    </div>
                    <div class="footer">
                        <p>© %s - Your Travel Companion</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, daysRemaining, customMessage, cityName, country,
                         startDate.format(formatter), appName);
    }

    private String buildTripStartEmailHtml(String cityName, String country, LocalDate startDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .footer { background: #333; color: white; padding: 20px; text-align: center; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 YOUR TRIP STARTS TODAY! 🎉</h1>
                    </div>
                    <div class="content">
                        <h2>Hello Traveler!</h2>
                        <p>The wait is over! Your exciting trip to <strong>%s, %s</strong> begins today!</p>
                        <p>📅 Date: %s</p>
                        <br>
                        <p>✈️ Safe travels and enjoy every moment!</p>
                    </div>
                    <div class="footer">
                        <p>© %s - Your Travel Companion</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(cityName, country, startDate.format(formatter), appName);
    }

    private String buildTripCompletedEmailHtml(String cityName, String country,
                                                LocalDate startDate, LocalDate endDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .footer { background: #333; color: white; padding: 20px; text-align: center; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Trip Completed!</h1>
                    </div>
                    <div class="content">
                        <h2>Welcome Back!</h2>
                        <p>Your trip to <strong>%s, %s</strong> has been marked as completed.</p>
                        <p>We hope you had an amazing experience!</p>
                        <br>
                        <p>🌟 Don't forget to rate your experience and plan your next adventure!</p>
                    </div>
                    <div class="footer">
                        <p>© %s - Your Travel Companion</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(cityName, country, appName);
    }

    private String buildTripCancelledEmailHtml(String cityName, String country) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: #e74c3c; color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .footer { background: #333; color: white; padding: 20px; text-align: center; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Trip Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Your trip to <strong>%s, %s</strong> has been cancelled.</p>
                        <p>We hope to see you planning another adventure soon!</p>
                    </div>
                    <div class="footer">
                        <p>© %s - Your Travel Companion</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(cityName, country, appName);
    }
}
