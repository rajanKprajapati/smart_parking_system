package com.smartparking.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model representing a parking facility / area owned by a provider.
 */
public class ParkingArea implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        ACTIVE, INACTIVE
    }

    private Long parkingAreaId;
    private Long providerId;
    private String parkingName;
    private String location;
    private String description;
    private BigDecimal pricePerHour;
    private String operatingDetails;
    private Status activeStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Additional transient/display fields
    private String providerName;
    private String providerEmail;
    private String providerPhone;
    private int totalSlots;
    private int availableSlots;

    public ParkingArea() {
        this.activeStatus = Status.ACTIVE;
        this.operatingDetails = "24/7 Operations";
        this.pricePerHour = BigDecimal.ZERO;
    }

    public ParkingArea(Long parkingAreaId, Long providerId, String parkingName, String location,
                       String description, BigDecimal pricePerHour, String operatingDetails, Status activeStatus) {
        this.parkingAreaId = parkingAreaId;
        this.providerId = providerId;
        this.parkingName = parkingName;
        this.location = location;
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.operatingDetails = operatingDetails;
        this.activeStatus = activeStatus != null ? activeStatus : Status.ACTIVE;
    }

    // Getters and Setters
    public Long getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(Long parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getOperatingDetails() {
        return operatingDetails;
    }

    public void setOperatingDetails(String operatingDetails) {
        this.operatingDetails = operatingDetails;
    }

    public Status getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Status activeStatus) {
        this.activeStatus = activeStatus;
    }

    public void setActiveStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.activeStatus = Status.valueOf(statusStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.activeStatus = Status.ACTIVE;
            }
        }
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

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    public String getProviderPhone() {
        return providerPhone;
    }

    public void setProviderPhone(String providerPhone) {
        this.providerPhone = providerPhone;
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

    public boolean isActive() {
        return Status.ACTIVE.equals(this.activeStatus);
    }
}
