package com.smartparking.service;

import com.smartparking.dao.ParkingAreaDao;
import com.smartparking.dao.ParkingSlotDao;
import com.smartparking.model.ParkingArea;
import com.smartparking.model.ParkingSlot;
import com.smartparking.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing parking facilities, slots, search, and availability checks.
 */
public class ParkingService {

    private static final Logger LOGGER = Logger.getLogger(ParkingService.class.getName());
    private final ParkingAreaDao parkingAreaDao;
    private final ParkingSlotDao parkingSlotDao;

    public ParkingService() {
        this.parkingAreaDao = new ParkingAreaDao();
        this.parkingSlotDao = new ParkingSlotDao();
    }

    public ParkingService(ParkingAreaDao parkingAreaDao, ParkingSlotDao parkingSlotDao) {
        this.parkingAreaDao = parkingAreaDao;
        this.parkingSlotDao = parkingSlotDao;
    }

    // --------------------------------------------------------------------------
    // Parking Area Management
    // --------------------------------------------------------------------------

    public String addParkingArea(ParkingArea area) {
        if (area == null) return "Invalid parking area data.";
        if (!ValidationUtil.isNotEmpty(area.getParkingName())) {
            return "Parking name is required.";
        }
        if (!ValidationUtil.isNotEmpty(area.getLocation())) {
            return "Parking location/address is required.";
        }
        if (area.getPricePerHour() == null || area.getPricePerHour().compareTo(BigDecimal.ZERO) < 0) {
            return "Price per hour must be zero or a positive amount.";
        }
        if (area.getProviderId() == null) {
            return "Provider ID is missing.";
        }

        try {
            Long id = parkingAreaDao.create(area);
            return id != null ? null : "Failed to create parking area.";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating parking area", e);
            return "Database error creating parking area.";
        }
    }

    public String updateParkingArea(ParkingArea area) {
        if (area == null || area.getParkingAreaId() == null || area.getProviderId() == null) {
            return "Invalid parking area update request.";
        }
        if (!ValidationUtil.isNotEmpty(area.getParkingName())) {
            return "Parking name is required.";
        }
        if (!ValidationUtil.isNotEmpty(area.getLocation())) {
            return "Location is required.";
        }
        if (area.getPricePerHour() == null || area.getPricePerHour().compareTo(BigDecimal.ZERO) < 0) {
            return "Price per hour must be valid.";
        }

        // Ownership check
        if (!parkingAreaDao.isOwnedByProvider(area.getParkingAreaId(), area.getProviderId())) {
            return "Access denied: You do not own this parking facility.";
        }

        try {
            boolean updated = parkingAreaDao.update(area);
            return updated ? null : "Failed to update parking area.";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating parking area: " + area.getParkingAreaId(), e);
            return "Database error updating parking area.";
        }
    }

    public boolean toggleAreaStatus(Long areaId, Long providerId, ParkingArea.Status status) {
        if (areaId == null || providerId == null || status == null) return false;
        try {
            return parkingAreaDao.toggleActiveStatus(areaId, providerId, status);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling area status", e);
            return false;
        }
    }

    public ParkingArea getAreaById(Long areaId) {
        return parkingAreaDao.findById(areaId);
    }

    public List<ParkingArea> getProviderAreas(Long providerId) {
        if (providerId == null) return Collections.emptyList();
        return parkingAreaDao.findByProviderId(providerId);
    }

    public List<ParkingArea> searchParkingAreas(String keyword, BigDecimal maxPrice, Boolean onlyActive) {
        return parkingAreaDao.searchAndFilter(keyword, maxPrice, onlyActive);
    }

    // --------------------------------------------------------------------------
    // Slot Management
    // --------------------------------------------------------------------------

    public String addSlot(ParkingSlot slot, Long providerId) {
        if (slot == null || slot.getParkingAreaId() == null) {
            return "Invalid slot data.";
        }
        if (!ValidationUtil.isNotEmpty(slot.getSlotNumber())) {
            return "Slot number/identifier is required.";
        }

        // Check area provider ownership
        if (!parkingAreaDao.isOwnedByProvider(slot.getParkingAreaId(), providerId)) {
            return "Access denied: You cannot add slots to another provider's parking area.";
        }

        // Check slot uniqueness within this area
        if (parkingSlotDao.existsSlotNumberInArea(slot.getParkingAreaId(), slot.getSlotNumber(), null)) {
            return "Slot '" + slot.getSlotNumber().trim() + "' already exists in this parking area.";
        }

        try {
            Long slotId = parkingSlotDao.create(slot);
            return slotId != null ? null : "Failed to create slot.";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating parking slot", e);
            return "Database error creating parking slot.";
        }
    }

    public String updateSlot(ParkingSlot slot, Long providerId) {
        if (slot == null || slot.getSlotId() == null || slot.getParkingAreaId() == null) {
            return "Invalid slot update request.";
        }
        if (!ValidationUtil.isNotEmpty(slot.getSlotNumber())) {
            return "Slot number is required.";
        }

        if (!parkingAreaDao.isOwnedByProvider(slot.getParkingAreaId(), providerId)) {
            return "Access denied: You cannot manage slots for this parking area.";
        }

        if (parkingSlotDao.existsSlotNumberInArea(slot.getParkingAreaId(), slot.getSlotNumber(), slot.getSlotId())) {
            return "Slot '" + slot.getSlotNumber().trim() + "' already exists in this parking area.";
        }

        try {
            boolean updated = parkingSlotDao.update(slot);
            return updated ? null : "Failed to update slot.";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating slot: " + slot.getSlotId(), e);
            return "Database error updating slot.";
        }
    }

    public boolean toggleSlotStatus(Long slotId, Long providerId, ParkingSlot.Status status) {
        if (slotId == null || providerId == null || status == null) return false;
        ParkingSlot slot = parkingSlotDao.findById(slotId);
        if (slot == null) return false;
        if (!parkingAreaDao.isOwnedByProvider(slot.getParkingAreaId(), providerId)) {
            return false;
        }
        try {
            return parkingSlotDao.toggleSlotStatus(slotId, status);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling slot status", e);
            return false;
        }
    }

    public List<ParkingSlot> getAreaSlots(Long areaId) {
        if (areaId == null) return Collections.emptyList();
        return parkingSlotDao.findByAreaId(areaId);
    }

    public List<ParkingSlot> getAvailableSlots(Long areaId, LocalDateTime start, LocalDateTime end, String vehicleType) {
        if (areaId == null || start == null || end == null) return Collections.emptyList();
        return parkingSlotDao.findAvailableSlotsForInterval(areaId, start, end, vehicleType);
    }

    public ParkingSlot getSlotById(Long slotId) {
        return parkingSlotDao.findById(slotId);
    }
}
