package com.smartparking.dao;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.model.ParkingArea;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for parking_areas table.
 */
public class ParkingAreaDao {

    private static final Logger LOGGER = Logger.getLogger(ParkingAreaDao.class.getName());

    public Long create(ParkingArea area) throws SQLException {
        String sql = "INSERT INTO parking_areas (provider_id, parking_name, location, description, price_per_hour, operating_details, active_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, area.getProviderId());
            ps.setString(2, area.getParkingName());
            ps.setString(3, area.getLocation());
            ps.setString(4, area.getDescription());
            ps.setBigDecimal(5, area.getPricePerHour());
            ps.setString(6, area.getOperatingDetails());
            ps.setString(7, area.getActiveStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        area.setParkingAreaId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting parking area: " + area.getParkingName(), e);
            throw e;
        }
        return null;
    }

    public boolean update(ParkingArea area) throws SQLException {
        // Enforces provider ownership check in SQL (provider_id = ?)
        String sql = "UPDATE parking_areas SET parking_name = ?, location = ?, description = ?, " +
                     "price_per_hour = ?, operating_details = ?, active_status = ? " +
                     "WHERE parking_area_id = ? AND provider_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, area.getParkingName());
            ps.setString(2, area.getLocation());
            ps.setString(3, area.getDescription());
            ps.setBigDecimal(4, area.getPricePerHour());
            ps.setString(5, area.getOperatingDetails());
            ps.setString(6, area.getActiveStatus().name());
            ps.setLong(7, area.getParkingAreaId());
            ps.setLong(8, area.getProviderId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating parking area: " + area.getParkingAreaId(), e);
            throw e;
        }
    }

    public ParkingArea findById(Long areaId) {
        if (areaId == null) return null;
        String sql = "SELECT pa.*, u.full_name AS provider_name, u.email AS provider_email, u.phone_number AS provider_phone, " +
                     "(SELECT COUNT(*) FROM parking_slots ps WHERE ps.parking_area_id = pa.parking_area_id AND ps.active_status = 'ACTIVE') AS total_slots " +
                     "FROM parking_areas pa " +
                     "JOIN users u ON pa.provider_id = u.user_id " +
                     "WHERE pa.parking_area_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding parking area by ID: " + areaId, e);
        }
        return null;
    }

    public List<ParkingArea> findByProviderId(Long providerId) {
        List<ParkingArea> list = new ArrayList<>();
        if (providerId == null) return list;
        String sql = "SELECT pa.*, u.full_name AS provider_name, u.email AS provider_email, u.phone_number AS provider_phone, " +
                     "(SELECT COUNT(*) FROM parking_slots ps WHERE ps.parking_area_id = pa.parking_area_id) AS total_slots " +
                     "FROM parking_areas pa " +
                     "JOIN users u ON pa.provider_id = u.user_id " +
                     "WHERE pa.provider_id = ? " +
                     "ORDER BY pa.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding parking areas for provider ID: " + providerId, e);
        }
        return list;
    }

    public List<ParkingArea> searchAndFilter(String keyword, BigDecimal maxPrice, Boolean onlyActive) {
        List<ParkingArea> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT pa.*, u.full_name AS provider_name, u.email AS provider_email, u.phone_number AS provider_phone, " +
            "(SELECT COUNT(*) FROM parking_slots ps WHERE ps.parking_area_id = pa.parking_area_id AND ps.active_status = 'ACTIVE') AS total_slots " +
            "FROM parking_areas pa " +
            "JOIN users u ON pa.provider_id = u.user_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (onlyActive != null && onlyActive) {
            sql.append("AND pa.active_status = 'ACTIVE' ");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(pa.parking_name) LIKE ? OR LOWER(pa.location) LIKE ? OR LOWER(pa.description) LIKE ?) ");
            String term = "%" + keyword.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND pa.price_per_hour <= ? ");
            params.add(maxPrice);
        }

        sql.append("ORDER BY pa.parking_name ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error searching parking areas", e);
        }
        return list;
    }

    public boolean toggleActiveStatus(Long areaId, Long providerId, ParkingArea.Status status) throws SQLException {
        String sql = "UPDATE parking_areas SET active_status = ? WHERE parking_area_id = ? AND provider_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, areaId);
            ps.setLong(3, providerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling parking area status", e);
            throw e;
        }
    }

    public boolean isOwnedByProvider(Long areaId, Long providerId) {
        if (areaId == null || providerId == null) return false;
        String sql = "SELECT 1 FROM parking_areas WHERE parking_area_id = ? AND provider_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, areaId);
            ps.setLong(2, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking area provider ownership", e);
            return false;
        }
    }

    private ParkingArea mapRow(ResultSet rs) throws SQLException {
        ParkingArea area = new ParkingArea();
        area.setParkingAreaId(rs.getLong("parking_area_id"));
        area.setProviderId(rs.getLong("provider_id"));
        area.setParkingName(rs.getString("parking_name"));
        area.setLocation(rs.getString("location"));
        area.setDescription(rs.getString("description"));
        area.setPricePerHour(rs.getBigDecimal("price_per_hour"));
        area.setOperatingDetails(rs.getString("operating_details"));
        area.setActiveStatus(rs.getString("active_status"));
        area.setCreatedAt(rs.getTimestamp("created_at"));
        area.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            area.setProviderName(rs.getString("provider_name"));
            area.setProviderEmail(rs.getString("provider_email"));
            area.setProviderPhone(rs.getString("provider_phone"));
        } catch (SQLException ignored) {}

        try {
            area.setTotalSlots(rs.getInt("total_slots"));
        } catch (SQLException ignored) {}

        return area;
    }
}
