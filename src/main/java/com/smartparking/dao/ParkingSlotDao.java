package com.smartparking.dao;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.model.ParkingSlot;
import com.smartparking.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for parking_slots table.
 */
public class ParkingSlotDao {

    private static final Logger LOGGER = Logger.getLogger(ParkingSlotDao.class.getName());

    public Long create(ParkingSlot slot) throws SQLException {
        String sql = "INSERT INTO parking_slots (parking_area_id, slot_number, vehicle_type, active_status) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, slot.getParkingAreaId());
            ps.setString(2, slot.getSlotNumber().trim());
            ps.setString(3, slot.getVehicleType().name());
            ps.setString(4, slot.getActiveStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        slot.setSlotId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating parking slot: " + slot.getSlotNumber(), e);
            throw e;
        }
        return null;
    }

    public boolean update(ParkingSlot slot) throws SQLException {
        String sql = "UPDATE parking_slots SET slot_number = ?, vehicle_type = ?, active_status = ? " +
                     "WHERE slot_id = ? AND parking_area_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getSlotNumber().trim());
            ps.setString(2, slot.getVehicleType().name());
            ps.setString(3, slot.getActiveStatus().name());
            ps.setLong(4, slot.getSlotId());
            ps.setLong(5, slot.getParkingAreaId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating parking slot: " + slot.getSlotId(), e);
            throw e;
        }
    }

    public ParkingSlot findById(Long slotId) {
        if (slotId == null) return null;
        String sql = "SELECT ps.*, pa.parking_name " +
                     "FROM parking_slots ps " +
                     "JOIN parking_areas pa ON ps.parking_area_id = pa.parking_area_id " +
                     "WHERE ps.slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding slot by ID: " + slotId, e);
        }
        return null;
    }

    public List<ParkingSlot> findByAreaId(Long areaId) {
        List<ParkingSlot> list = new ArrayList<>();
        if (areaId == null) return list;
        String sql = "SELECT ps.*, pa.parking_name " +
                     "FROM parking_slots ps " +
                     "JOIN parking_areas pa ON ps.parking_area_id = pa.parking_area_id " +
                     "WHERE ps.parking_area_id = ? " +
                     "ORDER BY ps.slot_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding slots for area ID: " + areaId, e);
        }
        return list;
    }

    /**
     * Finds all active slots in a parking area that are NOT booked for the given interval.
     */
    public List<ParkingSlot> findAvailableSlotsForInterval(Long areaId, LocalDateTime start, LocalDateTime end, String vehicleType) {
        List<ParkingSlot> list = new ArrayList<>();
        if (areaId == null || start == null || end == null) return list;

        StringBuilder sql = new StringBuilder(
            "SELECT ps.*, pa.parking_name " +
            "FROM parking_slots ps " +
            "JOIN parking_areas pa ON ps.parking_area_id = pa.parking_area_id " +
            "WHERE ps.parking_area_id = ? " +
            "AND ps.active_status = 'ACTIVE' " +
            "AND pa.active_status = 'ACTIVE' " +
            "AND ps.slot_id NOT IN (" +
            "    SELECT b.slot_id FROM bookings b " +
            "    WHERE b.parking_area_id = ? " +
            "    AND b.booking_status = 'CONFIRMED' " +
            "    AND b.start_time < ? " +
            "    AND b.end_time > ? " +
            ") "
        );

        if (vehicleType != null && !vehicleType.trim().isEmpty() && !"ANY".equalsIgnoreCase(vehicleType)) {
            sql.append("AND (ps.vehicle_type = ? OR ps.vehicle_type = 'ANY') ");
        }

        sql.append("ORDER BY ps.slot_number ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, areaId);
            ps.setLong(2, areaId);
            ps.setTimestamp(3, DateTimeUtil.toTimestamp(end));
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(start));

            if (vehicleType != null && !vehicleType.trim().isEmpty() && !"ANY".equalsIgnoreCase(vehicleType)) {
                ps.setString(5, vehicleType.toUpperCase().trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error querying available slots for interval", e);
        }
        return list;
    }

    /**
     * Checks if a specific slot is available for an interval, optionally reusing an existing connection (for transactions).
     */
    public boolean isSlotAvailableInInterval(Long slotId, LocalDateTime start, LocalDateTime end, Long excludeBookingId, Connection externalConn) throws SQLException {
        if (slotId == null || start == null || end == null) return false;

        String sql = "SELECT 1 FROM bookings WHERE slot_id = ? AND booking_status = 'CONFIRMED' " +
                     "AND start_time < ? AND end_time > ? " +
                     (excludeBookingId != null ? "AND booking_id <> ? " : "") +
                     "LIMIT 1 FOR UPDATE"; // Row-level lock during transaction check

        Connection conn = (externalConn != null) ? externalConn : DatabaseConnection.getConnection();
        boolean closeConn = (externalConn == null);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, slotId);
            ps.setTimestamp(2, DateTimeUtil.toTimestamp(end));
            ps.setTimestamp(3, DateTimeUtil.toTimestamp(start));
            if (excludeBookingId != null) {
                ps.setLong(4, excludeBookingId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                // If a conflicting row exists, it is NOT available
                return !rs.next();
            }
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
    }

    public boolean toggleSlotStatus(Long slotId, ParkingSlot.Status status) throws SQLException {
        String sql = "UPDATE parking_slots SET active_status = ? WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, slotId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling slot active status", e);
            throw e;
        }
    }

    public boolean existsSlotNumberInArea(Long areaId, String slotNumber, Long excludeSlotId) {
        if (areaId == null || slotNumber == null) return false;
        StringBuilder sql = new StringBuilder("SELECT 1 FROM parking_slots WHERE parking_area_id = ? AND LOWER(slot_number) = ? ");
        if (excludeSlotId != null) {
            sql.append("AND slot_id <> ? ");
        }
        sql.append("LIMIT 1");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, areaId);
            ps.setString(2, slotNumber.toLowerCase().trim());
            if (excludeSlotId != null) {
                ps.setLong(3, excludeSlotId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking slot uniqueness", e);
            return false;
        }
    }

    private ParkingSlot mapRow(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotId(rs.getLong("slot_id"));
        slot.setParkingAreaId(rs.getLong("parking_area_id"));
        slot.setSlotNumber(rs.getString("slot_number"));
        slot.setVehicleType(rs.getString("vehicle_type"));
        slot.setActiveStatus(rs.getString("active_status"));
        slot.setCreatedAt(rs.getTimestamp("created_at"));
        slot.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            slot.setParkingName(rs.getString("parking_name"));
        } catch (SQLException ignored) {}

        return slot;
    }
}
