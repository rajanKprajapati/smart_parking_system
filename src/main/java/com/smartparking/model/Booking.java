package com.smartparking.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Model representing a customer's parking reservation.
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum BookingStatus {
        CONFIRMED, CANCELLED, COMPLETED, EXPIRED
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED
    }

    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private Long parkingAreaId;
    private Long slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal durationHours;
    private BigDecimal calculatedFee;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime paidAt;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Display / join fields
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String parkingName;
    private String parkingLocation;
    private String slotNumber;
    private String vehicleType;
    private BigDecimal pricePerHour;
    private Long providerId;
    private String providerName;

    public Booking() {
        this.bookingStatus = BookingStatus.CONFIRMED;
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentMethod = "UPI_QR";
        this.durationHours = BigDecimal.ZERO;
        this.calculatedFee = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(Long parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(BigDecimal durationHours) {
        this.durationHours = durationHours;
    }

    public BigDecimal getCalculatedFee() {
        return calculatedFee;
    }

    public void setCalculatedFee(BigDecimal calculatedFee) {
        this.calculatedFee = calculatedFee;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public void setBookingStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.bookingStatus = BookingStatus.valueOf(statusStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.bookingStatus = BookingStatus.CONFIRMED;
            }
        }
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getParkingLocation() {
        return parkingLocation;
    }

    public void setParkingLocation(String parkingLocation) {
        this.parkingLocation = parkingLocation;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    // Helper status checks
    public boolean isConfirmed() {
        return BookingStatus.CONFIRMED.equals(this.bookingStatus);
    }

    public boolean isPaid() {
        return PaymentStatus.PAID.equals(this.paymentStatus);
    }

    public boolean isPaymentPending() {
        return PaymentStatus.PENDING.equals(this.paymentStatus);
    }

    public boolean isPaymentFailed() {
        return PaymentStatus.FAILED.equals(this.paymentStatus);
    }

    public boolean isCancelled() {
        return BookingStatus.CANCELLED.equals(this.bookingStatus);
    }

    public boolean isCompleted() {
        return BookingStatus.COMPLETED.equals(this.bookingStatus);
    }

    public boolean isExpired() {
        return BookingStatus.EXPIRED.equals(this.bookingStatus);
    }

    public boolean isEligibleForCancellation() {
        // Can only cancel if currently CONFIRMED and the start time is in the future
        return isConfirmed() && startTime != null && LocalDateTime.now().isBefore(startTime);
    }

    public boolean isCurrentlyActive() {
        if (!isConfirmed() || startTime == null || endTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }

    public boolean isUpcoming() {
        if (!isConfirmed() || startTime == null) return false;
        return LocalDateTime.now().isBefore(startTime);
    }
}
