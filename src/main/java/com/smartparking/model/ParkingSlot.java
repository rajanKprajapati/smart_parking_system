package com.smartparking.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model representing an individual parking slot/bay inside a parking area.
 */
public class ParkingSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum VehicleType {
        CAR, BIKE, SUV, EV, ANY
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    private Long slotId;
    private Long parkingAreaId;
    private String slotNumber;
    private VehicleType vehicleType;
    private Status activeStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient display fields
    private String parkingName;
    private boolean currentlyBooked; // True if an active reservation exists for the query interval

    public ParkingSlot() {
        this.vehicleType = VehicleType.CAR;
        this.activeStatus = Status.ACTIVE;
    }

    public ParkingSlot(Long slotId, Long parkingAreaId, String slotNumber, VehicleType vehicleType, Status activeStatus) {
        this.slotId = slotId;
        this.parkingAreaId = parkingAreaId;
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType != null ? vehicleType : VehicleType.CAR;
        this.activeStatus = activeStatus != null ? activeStatus : Status.ACTIVE;
    }

    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(Long parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setVehicleType(String typeStr) {
        if (typeStr != null) {
            try {
                this.vehicleType = VehicleType.valueOf(typeStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.vehicleType = VehicleType.CAR;
            }
        }
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

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public boolean isCurrentlyBooked() {
        return currentlyBooked;
    }

    public void setCurrentlyBooked(boolean currentlyBooked) {
        this.currentlyBooked = currentlyBooked;
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.activeStatus);
    }
}
