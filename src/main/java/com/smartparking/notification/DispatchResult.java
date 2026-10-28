package com.smartparking.notification;

import com.smartparking.model.Notification;

/**
 * Result of an email or SMS transmission attempt.
 */
public class DispatchResult {

    private final Notification.DeliveryStatus status;
    private final String safeErrorSummary;

    public DispatchResult(Notification.DeliveryStatus status, String safeErrorSummary) {
        this.status = status;
        this.safeErrorSummary = safeErrorSummary;
    }

    public static DispatchResult sent() {
        return new DispatchResult(Notification.DeliveryStatus.SENT, null);
    }

    public static DispatchResult simulated() {
        return new DispatchResult(Notification.DeliveryStatus.SIMULATED, "Simulated in Development Mode (no live credentials configured)");
    }

    public static DispatchResult failed(String safeErrorSummary) {
        return new DispatchResult(Notification.DeliveryStatus.FAILED, safeErrorSummary);
    }

    public Notification.DeliveryStatus getStatus() {
        return status;
    }

    public String getSafeErrorSummary() {
        return safeErrorSummary;
    }

    public boolean isSuccess() {
        return Notification.DeliveryStatus.SENT.equals(status) || Notification.DeliveryStatus.SIMULATED.equals(status);
    }
}
