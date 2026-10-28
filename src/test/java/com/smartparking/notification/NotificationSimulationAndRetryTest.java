package com.smartparking.notification;

import com.smartparking.model.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationSimulationAndRetryTest {

    @Test
    @DisplayName("Email Service: Development mode marks message as SIMULATED, not SENT")
    void testEmailDevelopmentSimulation() {
        EmailService service = new EmailService();
        DispatchResult result = service.sendEmail("test@example.com", "Test Subject", "Test Body");

        assertNotNull(result);
        assertEquals(Notification.DeliveryStatus.SIMULATED, result.getStatus());
        assertNotEquals(Notification.DeliveryStatus.SENT, result.getStatus());
        assertTrue(result.isSuccess());
        assertNotNull(result.getSafeErrorSummary());
        assertTrue(result.getSafeErrorSummary().contains("Simulated"));
    }

    @Test
    @DisplayName("SMS Service: Development mode marks message as SIMULATED, not SENT")
    void testSmsDevelopmentSimulation() {
        SmsService service = new SmsService();
        DispatchResult result = service.sendSms("+15551234567", "Hello Driver");

        assertNotNull(result);
        assertEquals(Notification.DeliveryStatus.SIMULATED, result.getStatus());
        assertNotEquals(Notification.DeliveryStatus.SENT, result.getStatus());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Notification Retry: isEligibleForRetry requires FAILED/RETRYING and attempts < maxRetries")
    void testRetryEligibility() {
        Notification n = new Notification();
        n.setMaxRetries(3);

        // PENDING -> Not eligible for manual retry
        n.setDeliveryStatus(Notification.DeliveryStatus.PENDING);
        assertFalse(n.isEligibleForRetry());

        // SENT -> Completed, not eligible
        n.setDeliveryStatus(Notification.DeliveryStatus.SENT);
        assertFalse(n.isEligibleForRetry());

        // FAILED with 1 attempt -> Eligible!
        n.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        n.setAttemptCount(1);
        assertTrue(n.isEligibleForRetry());

        // FAILED with 3 attempts (maxed out) -> Ineligible!
        n.setAttemptCount(3);
        assertFalse(n.isEligibleForRetry());
    }
}
