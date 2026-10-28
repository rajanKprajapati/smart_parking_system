package com.smartparking.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Model representing aggregated statistics and basic analytics for the Provider Dashboard.
 * Accurately distinguishes calculated booking value from collected payment revenue.
 */
public class DashboardStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalParkingAreas;
    private int totalSlots;
    private int availableSlots;
    private int upcomingBookings;
    private int activeBookings;
    private int completedBookings;
    private int cancelledBookings;
    private int expiredBookings;
    private BigDecimal totalEligibleBookingValue;

    public DashboardStats() {
        this.totalParkingAreas = 0;
        this.totalSlots = 0;
        this.availableSlots = 0;
        this.upcomingBookings = 0;
        this.activeBookings = 0;
        this.completedBookings = 0;
        this.cancelledBookings = 0;
        this.expiredBookings = 0;
        this.totalEligibleBookingValue = BigDecimal.ZERO;
    }

    public int getTotalParkingAreas() {
        return totalParkingAreas;
    }

    public void setTotalParkingAreas(int totalParkingAreas) {
        this.totalParkingAreas = totalParkingAreas;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public int getUpcomingBookings() {
        return upcomingBookings;
    }

    public void setUpcomingBookings(int upcomingBookings) {
        this.upcomingBookings = upcomingBookings;
    }

    public int getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(int activeBookings) {
        this.activeBookings = activeBookings;
    }

    public int getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(int completedBookings) {
        this.completedBookings = completedBookings;
    }

    public int getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(int cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public int getExpiredBookings() {
        return expiredBookings;
    }

    public void setExpiredBookings(int expiredBookings) {
        this.expiredBookings = expiredBookings;
    }

    public BigDecimal getTotalEligibleBookingValue() {
        return totalEligibleBookingValue;
    }

    public void setTotalEligibleBookingValue(BigDecimal totalEligibleBookingValue) {
        this.totalEligibleBookingValue = totalEligibleBookingValue != null ? totalEligibleBookingValue : BigDecimal.ZERO;
    }

    public int getTotalBookings() {
        return upcomingBookings + activeBookings + completedBookings + cancelledBookings + expiredBookings;
    }
}
