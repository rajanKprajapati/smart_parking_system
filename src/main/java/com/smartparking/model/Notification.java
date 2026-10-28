package com.smartparking.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Model representing an email or SMS notification dispatched or recorded by the system.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Channel {
        EMAIL, SMS
    }

    public enum NotificationType {
        BOOKING_CONFIRMATION, BOOKING_EXPIRY, BOOKING_CANCELLED
    }

    public enum DeliveryStatus {
        PENDING, SENT, SIMULATED, FAILED, RETRYING
    }

    private Long notificationId;
    private Long bookingId;
    private Long recipientUserId;
    private String recipientContact;
    private Channel channel;
    private NotificationType notificationType;
    private String subject;
    private String message;
    private DeliveryStatus deliveryStatus;
    private int attemptCount;
    private int maxRetries;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime sentAt;
    private String safeErrorSummary;
    private Timestamp createdAt;

    // Display fields
    private String bookingReference;
    private String recipientName;
    private String parkingName;
    private String slotNumber;

    public Notification() {
        this.deliveryStatus = DeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.maxRetries = 3;
    }

    public Notification(Long bookingId, Long recipientUserId, String recipientContact,
                        Channel channel, NotificationType notificationType, String subject, String message) {
        this.bookingId = bookingId;
        this.recipientUserId = recipientUserId;
        this.recipientContact = recipientContact;
        this.channel = channel;
        this.notificationType = notificationType;
        this.subject = subject;
        this.message = message;
        this.deliveryStatus = DeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.maxRetries = 3;
    }

    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientContact() {
        return recipientContact;
    }

    public void setRecipientContact(String recipientContact) {
        this.recipientContact = recipientContact;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setChannel(String channelStr) {
        if (channelStr != null) {
            try {
                this.channel = Channel.valueOf(channelStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.channel = Channel.EMAIL;
            }
        }
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public void setNotificationType(String typeStr) {
        if (typeStr != null) {
            try {
                this.notificationType = NotificationType.valueOf(typeStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.notificationType = NotificationType.BOOKING_EXPIRY;
            }
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void setDeliveryStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.deliveryStatus = DeliveryStatus.valueOf(statusStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.deliveryStatus = DeliveryStatus.PENDING;
            }
        }
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getSafeErrorSummary() {
        return safeErrorSummary;
    }

    public void setSafeErrorSummary(String safeErrorSummary) {
        this.safeErrorSummary = safeErrorSummary;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public boolean isEligibleForRetry() {
        return (DeliveryStatus.FAILED.equals(this.deliveryStatus) || DeliveryStatus.RETRYING.equals(this.deliveryStatus))
                && this.attemptCount < this.maxRetries;
    }
}
