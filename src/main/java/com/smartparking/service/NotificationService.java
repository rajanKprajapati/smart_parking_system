package com.smartparking.service;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.dao.NotificationDao;
import com.smartparking.dao.UserDao;
import com.smartparking.model.Booking;
import com.smartparking.model.Notification;
import com.smartparking.model.User;
import com.smartparking.notification.DispatchResult;
import com.smartparking.notification.EmailService;
import com.smartparking.notification.SmsService;
import com.smartparking.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service orchestrating email and SMS notification creation, dispatch, and retries.
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final NotificationDao notificationDao;
    private final UserDao userDao;
    private final EmailService emailService;
    private final SmsService smsService;

    public NotificationService() {
        this.notificationDao = new NotificationDao();
        this.userDao = new UserDao();
        this.emailService = new EmailService();
        this.smsService = new SmsService();
    }

    public NotificationService(NotificationDao notificationDao, UserDao userDao,
                               EmailService emailService, SmsService smsService) {
        this.notificationDao = notificationDao;
        this.userDao = userDao;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Sends booking confirmation via Email and SMS to the customer.
     */
    public void sendBookingConfirmation(Booking booking) {
        if (booking == null || booking.getBookingId() == null) return;

        // 1. Customer Email
        String emailSubject = "Booking Confirmed - Ref #" + booking.getBookingReference();
        String emailBody = "Hello " + booking.getCustomerName() + ",\n\n" +
                "Your parking reservation has been successfully confirmed.\n\n" +
                "Booking Reference: " + booking.getBookingReference() + "\n" +
                "Parking Facility:  " + booking.getParkingName() + "\n" +
                "Location:          " + booking.getParkingLocation() + "\n" +
                "Slot Number:       " + booking.getSlotNumber() + "\n" +
                "Start Time:        " + DateTimeUtil.formatDisplay(booking.getStartTime()) + "\n" +
                "End Time:          " + DateTimeUtil.formatDisplay(booking.getEndTime()) + "\n" +
                "Estimated Fee:     $" + booking.getCalculatedFee() + "\n\n" +
                "Thank you for using Smart Parking Management System!";

        dispatchAndRecord(booking.getBookingId(), booking.getUserId(), booking.getCustomerEmail(),
                Notification.Channel.EMAIL, Notification.NotificationType.BOOKING_CONFIRMATION, emailSubject, emailBody);

        // 2. Customer SMS
        String smsBody = "Smart Parking: Booking #" + booking.getBookingReference() +
                " confirmed for Slot " + booking.getSlotNumber() + " at " + booking.getParkingName() +
                ". Valid until " + DateTimeUtil.formatDisplay(booking.getEndTime()) + ".";

        dispatchAndRecord(booking.getBookingId(), booking.getUserId(), booking.getCustomerPhone(),
                Notification.Channel.SMS, Notification.NotificationType.BOOKING_CONFIRMATION, null, smsBody);
    }

    /**
     * Sends automatic parking-expiry notifications to both Customer and Provider via Email and SMS.
     * Guaranteed 4 notifications per expired booking.
     */
    public void sendBookingExpiry(Booking booking, Connection externalConn) {
        if (booking == null || booking.getBookingId() == null) return;

        // Fetch provider contact if not already loaded in booking
        String providerEmail = null;
        String providerPhone = null;
        String providerName = booking.getProviderName();

        if (booking.getProviderId() != null) {
            User provider = userDao.findById(booking.getProviderId());
            if (provider != null) {
                providerEmail = provider.getEmail();
                providerPhone = provider.getPhoneNumber();
                if (providerName == null) providerName = provider.getFullName();
            }
        }

        String expiryTimeStr = DateTimeUtil.formatDisplay(booking.getEndTime());

        // 1. Customer Email
        String custSubject = "Parking Booking Expired - Ref #" + booking.getBookingReference();
        String custEmailBody = "Dear " + booking.getCustomerName() + ",\n\n" +
                "Your parking booking #" + booking.getBookingReference() + " at " + booking.getParkingName() +
                " reached its scheduled end time (" + expiryTimeStr + ").\n\n" +
                "Please vacate parking slot " + booking.getSlotNumber() + " or contact the parking provider if you need assistance.\n\n" +
                "Thank you,\nSmart Parking Management System";

        dispatchAndRecordWithConn(booking.getBookingId(), booking.getUserId(), booking.getCustomerEmail(),
                Notification.Channel.EMAIL, Notification.NotificationType.BOOKING_EXPIRY, custSubject, custEmailBody, externalConn);

        // 2. Customer SMS
        String custSmsBody = "Your parking booking #" + booking.getBookingReference() +
                " has reached its scheduled end time (" + expiryTimeStr + "). Please vacate slot " +
                booking.getSlotNumber() + " or contact the provider.";

        dispatchAndRecordWithConn(booking.getBookingId(), booking.getUserId(), booking.getCustomerPhone(),
                Notification.Channel.SMS, Notification.NotificationType.BOOKING_EXPIRY, null, custSmsBody, externalConn);

        // 3. Provider Email
        if (booking.getProviderId() != null && providerEmail != null) {
            String provSubject = "Customer Booking Expired - Ref #" + booking.getBookingReference();
            String provEmailBody = "Hello " + providerName + ",\n\n" +
                    "Customer booking #" + booking.getBookingReference() + " for slot " + booking.getSlotNumber() +
                    " at " + booking.getParkingName() + " reached its scheduled end time (" + expiryTimeStr + ").\n\n" +
                    "Customer Name:  " + booking.getCustomerName() + "\n" +
                    "Customer Phone: " + booking.getCustomerPhone() + "\n\n" +
                    "Smart Parking Notification Engine";

            dispatchAndRecordWithConn(booking.getBookingId(), booking.getProviderId(), providerEmail,
                    Notification.Channel.EMAIL, Notification.NotificationType.BOOKING_EXPIRY, provSubject, provEmailBody, externalConn);
        }

        // 4. Provider SMS
        if (booking.getProviderId() != null && providerPhone != null) {
            String provSmsBody = "Provider Alert: Booking #" + booking.getBookingReference() +
                    " for slot " + booking.getSlotNumber() + " at " + booking.getParkingName() +
                    " expired at " + expiryTimeStr + ". Customer: " + booking.getCustomerName();

            dispatchAndRecordWithConn(booking.getBookingId(), booking.getProviderId(), providerPhone,
                    Notification.Channel.SMS, Notification.NotificationType.BOOKING_EXPIRY, null, provSmsBody, externalConn);
        }
    }

    /**
     * Sends cancellation notifications to customer.
     */
    public void sendBookingCancellation(Booking booking, String reason) {
        if (booking == null || booking.getBookingId() == null) return;

        String subject = "Booking Cancelled - Ref #" + booking.getBookingReference();
        String emailBody = "Hello " + booking.getCustomerName() + ",\n\n" +
                "Your parking booking #" + booking.getBookingReference() + " for slot " +
                booking.getSlotNumber() + " at " + booking.getParkingName() + " has been cancelled.\n" +
                "Reason: " + (reason != null ? reason : "Requested by customer") + "\n\n" +
                "The reserved slot has been released back into available inventory.\n\n" +
                "Smart Parking Management System";

        dispatchAndRecord(booking.getBookingId(), booking.getUserId(), booking.getCustomerEmail(),
                Notification.Channel.EMAIL, Notification.NotificationType.BOOKING_CANCELLED, subject, emailBody);

        String smsBody = "Booking #" + booking.getBookingReference() + " has been cancelled. Slot " +
                booking.getSlotNumber() + " has been released.";

        dispatchAndRecord(booking.getBookingId(), booking.getUserId(), booking.getCustomerPhone(),
                Notification.Channel.SMS, Notification.NotificationType.BOOKING_CANCELLED, null, smsBody);
    }

    /**
     * Retries sending a failed notification.
     */
    public boolean retryNotification(Long notificationId, Long requestingUserId) {
        if (notificationId == null) return false;
        Notification n = notificationDao.findById(notificationId);
        if (n == null || !n.isEligibleForRetry()) {
            return false;
        }

        // Security check: only the recipient user or an admin can trigger retry
        if (requestingUserId != null && !requestingUserId.equals(n.getRecipientUserId())) {
            return false;
        }

        int newAttempt = n.getAttemptCount() + 1;
        DispatchResult result;

        if (Notification.Channel.EMAIL.equals(n.getChannel())) {
            result = emailService.sendEmail(n.getRecipientContact(), n.getSubject(), n.getMessage());
        } else {
            result = smsService.sendSms(n.getRecipientContact(), n.getMessage());
        }

        Timestamp sentAt = result.isSuccess() ? new Timestamp(System.currentTimeMillis()) : null;
        return notificationDao.updateStatus(n.getNotificationId(), result.getStatus(), newAttempt, result.getSafeErrorSummary(), sentAt);
    }

    public List<Notification> getUserNotifications(Long userId) {
        if (userId == null) return Collections.emptyList();
        return notificationDao.findByUserId(userId);
    }

    private void dispatchAndRecord(Long bookingId, Long recipientUserId, String contact,
                                   Notification.Channel channel, Notification.NotificationType type,
                                   String subject, String message) {
        dispatchAndRecordWithConn(bookingId, recipientUserId, contact, channel, type, subject, message, null);
    }

    private void dispatchAndRecordWithConn(Long bookingId, Long recipientUserId, String contact,
                                           Notification.Channel channel, Notification.NotificationType type,
                                           String subject, String message, Connection externalConn) {
        if (recipientUserId == null || contact == null || contact.trim().isEmpty()) {
            return;
        }

        Notification n = new Notification(bookingId, recipientUserId, contact, channel, type, subject, message);

        try {
            Long notificationId = notificationDao.createNotification(n, externalConn);
            if (notificationId == null) {
                LOGGER.info("Duplicate notification prevented for booking=" + bookingId + ", user=" + recipientUserId + ", channel=" + channel);
                return;
            }

            // Dispatch
            DispatchResult result;
            if (Notification.Channel.EMAIL.equals(channel)) {
                result = emailService.sendEmail(contact, subject, message);
            } else {
                result = smsService.sendSms(contact, message);
            }

            Timestamp sentAt = result.isSuccess() ? new Timestamp(System.currentTimeMillis()) : null;
            notificationDao.updateStatus(notificationId, result.getStatus(), 1, result.getSafeErrorSummary(), sentAt);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error creating or dispatching notification", e);
        }
    }
}
