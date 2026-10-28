package com.smartparking.notification;

import com.smartparking.config.AppConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for dispatching email notifications via SMTP (Jakarta Mail),
 * with transparent support for development simulation mode.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    public DispatchResult sendEmail(String toEmail, String subject, String bodyText) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            return DispatchResult.failed("Recipient email address is missing or blank");
        }

        // If mail is disabled or credentials are blank -> Development Simulation Mode
        if (!AppConfig.isMailEnabled() || AppConfig.getMailUsername().isEmpty() || AppConfig.getMailPassword().isEmpty()) {
            LOGGER.info("[SIMULATED EMAIL] To: " + toEmail + " | Subject: " + subject + "\nBody: " + bodyText);
            return DispatchResult.simulated();
        }

        // Real SMTP dispatch
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", AppConfig.getMailSmtpHost());
            props.put("mail.smtp.port", String.valueOf(AppConfig.getMailSmtpPort()));
            props.put("mail.smtp.auth", String.valueOf(AppConfig.isMailSmtpAuth()));
            props.put("mail.smtp.starttls.enable", String.valueOf(AppConfig.isMailSmtpStarttls()));
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(AppConfig.getMailUsername(), AppConfig.getMailPassword());
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.getMailFrom(), AppConfig.getMailFromName()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail.trim()));
            message.setSubject(subject, "UTF-8");
            message.setText(bodyText, "UTF-8");
            message.setSentDate(new Date());

            Transport.send(message);
            LOGGER.info("Email successfully sent to: " + toEmail);
            return DispatchResult.sent();
        } catch (Exception e) {
            String error = "SMTP delivery error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            LOGGER.log(Level.WARNING, "Failed to send email to " + toEmail, e);
            return DispatchResult.failed(error.length() > 250 ? error.substring(0, 250) : error);
        }
    }
}
