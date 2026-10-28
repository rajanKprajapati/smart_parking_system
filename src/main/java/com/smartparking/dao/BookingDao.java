package com.smartparking.dao;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.model.Booking;
import com.smartparking.model.DashboardStats;
import com.smartparking.util.DateTimeUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for bookings table.
 */
public class BookingDao {

    private static final Logger LOGGER = Logger.getLogger(BookingDao.class.getName());

    public Long createBooking(Booking booking, Connection conn) throws SQLException {
        String sql = "INSERT INTO bookings (booking_reference, user_id, parking_area_id, slot_id, " +
                     "start_time, end_time, duration_hours, calculated_fee, booking_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, booking.getBookingReference());
            ps.setLong(2, booking.getUserId());
            ps.setLong(3, booking.getParkingAreaId());
            ps.setLong(4, booking.getSlotId());
            ps.setTimestamp(5, DateTimeUtil.toTimestamp(booking.getStartTime()));
            ps.setTimestamp(6, DateTimeUtil.toTimestamp(booking.getEndTime()));
            ps.setBigDecimal(7, booking.getDurationHours());
            ps.setBigDecimal(8, booking.getCalculatedFee());
            ps.setString(9, booking.getBookingStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        booking.setBookingId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting booking: " + booking.getBookingReference(), e);
            throw e;
        }
        return null;
    }

    public Booking findById(Long bookingId) {
        if (bookingId == null) return null;
        String sql = baseBookingSelect() + "WHERE b.booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding booking by ID: " + bookingId, e);
        }
        return null;
    }

    public Booking findByReference(String reference) {
        if (reference == null) return null;
        String sql = baseBookingSelect() + "WHERE b.booking_reference = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reference.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding booking by reference: " + reference, e);
        }
        return null;
    }

    public List<Booking> findByUserId(Long userId, String statusFilter) {
        List<Booking> list = new ArrayList<>();
        if (userId == null) return list;
        StringBuilder sql = new StringBuilder(baseBookingSelect()).append("WHERE b.user_id = ? ");
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append("AND b.booking_status = ? ");
        }
        sql.append("ORDER BY b.start_time DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, userId);
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                ps.setString(2, statusFilter.toUpperCase().trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding bookings for user ID: " + userId, e);
        }
        return list;
    }

    public List<Booking> findByProviderId(Long providerId, String statusFilter) {
        List<Booking> list = new ArrayList<>();
        if (providerId == null) return list;
        StringBuilder sql = new StringBuilder(baseBookingSelect()).append("WHERE pa.provider_id = ? ");
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append("AND b.booking_status = ? ");
        }
        sql.append("ORDER BY b.start_time DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, providerId);
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                ps.setString(2, statusFilter.toUpperCase().trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding bookings for provider ID: " + providerId, e);
        }
        return list;
    }

    public boolean cancelBooking(Long bookingId, Long userId, String reason, Connection externalConn) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = 'CANCELLED', cancellation_reason = ?, " +
                     "cancelled_at = NOW() WHERE booking_id = ? AND user_id = ? AND booking_status = 'CONFIRMED' " +
                     "AND start_time > NOW()";

        Connection conn = (externalConn != null) ? externalConn : DatabaseConnection.getConnection();
        boolean closeConn = (externalConn == null);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason != null ? reason.trim() : "Cancelled by user");
            ps.setLong(2, bookingId);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
    }

    public boolean updateStatus(Long bookingId, Booking.BookingStatus status, Connection externalConn) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";
        Connection conn = (externalConn != null) ? externalConn : DatabaseConnection.getConnection();
        boolean closeConn = (externalConn == null);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, bookingId);
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Finds bookings whose end_time has passed and are still marked as CONFIRMED.
     * Uses row locking (FOR UPDATE) if called in a transaction to prevent race conditions.
     */
    public List<Booking> findExpiredEligibleBookings(LocalDateTime cutoffTime, Connection conn) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = baseBookingSelect() +
                     "WHERE b.booking_status = 'CONFIRMED' AND b.end_time <= ? " +
                     "ORDER BY b.end_time ASC FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, DateTimeUtil.toTimestamp(cutoffTime));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public DashboardStats getProviderDashboardStats(Long providerId) {
        DashboardStats stats = new DashboardStats();
        if (providerId == null) return stats;

        // 1. Total areas and total slots
        String slotSql = "SELECT COUNT(DISTINCT pa.parking_area_id) AS total_areas, " +
                         "COUNT(ps.slot_id) AS total_slots " +
                         "FROM parking_areas pa " +
                         "LEFT JOIN parking_slots ps ON pa.parking_area_id = ps.parking_area_id " +
                         "WHERE pa.provider_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(slotSql)) {
            ps.setLong(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalParkingAreas(rs.getInt("total_areas"));
                    stats.setTotalSlots(rs.getInt("total_slots"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating slot stats for provider: " + providerId, e);
        }

        // 2. Count bookings by status and calculate eligible booking value
        String bookingSql = "SELECT " +
                "COUNT(CASE WHEN b.booking_status = 'CONFIRMED' AND b.start_time > NOW() THEN 1 END) AS upcoming_count, " +
                "COUNT(CASE WHEN b.booking_status = 'CONFIRMED' AND b.start_time <= NOW() AND b.end_time > NOW() THEN 1 END) AS active_count, " +
                "COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed_count, " +
                "COUNT(CASE WHEN b.booking_status = 'CANCELLED' THEN 1 END) AS cancelled_count, " +
                "COUNT(CASE WHEN b.booking_status = 'EXPIRED' THEN 1 END) AS expired_count, " +
                "COALESCE(SUM(CASE WHEN b.booking_status IN ('CONFIRMED', 'COMPLETED', 'EXPIRED') THEN b.calculated_fee ELSE 0 END), 0) AS total_value " +
                "FROM bookings b " +
                "JOIN parking_areas pa ON b.parking_area_id = pa.parking_area_id " +
                "WHERE pa.provider_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(bookingSql)) {
            ps.setLong(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setUpcomingBookings(rs.getInt("upcoming_count"));
                    stats.setActiveBookings(rs.getInt("active_count"));
                    stats.setCompletedBookings(rs.getInt("completed_count"));
                    stats.setCancelledBookings(rs.getInt("cancelled_count"));
                    stats.setExpiredBookings(rs.getInt("expired_count"));
                    stats.setTotalEligibleBookingValue(rs.getBigDecimal("total_value"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating booking stats for provider: " + providerId, e);
        }

        // 3. Currently available slots = total active slots minus slots currently in an active booking
        String availSql = "SELECT COUNT(ps.slot_id) AS available_slots " +
                          "FROM parking_slots ps " +
                          "JOIN parking_areas pa ON ps.parking_area_id = pa.parking_area_id " +
                          "WHERE pa.provider_id = ? AND ps.active_status = 'ACTIVE' AND pa.active_status = 'ACTIVE' " +
                          "AND ps.slot_id NOT IN (" +
                          "    SELECT b.slot_id FROM bookings b " +
                          "    WHERE b.booking_status = 'CONFIRMED' AND b.start_time <= NOW() AND b.end_time > NOW()" +
                          ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(availSql)) {
            ps.setLong(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setAvailableSlots(rs.getInt("available_slots"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating available slots for provider: " + providerId, e);
        }

        return stats;
    }

    private String baseBookingSelect() {
        return "SELECT b.*, " +
               "u.full_name AS customer_name, u.email AS customer_email, u.phone_number AS customer_phone, " +
               "pa.parking_name, pa.location AS parking_location, pa.price_per_hour, " +
               "pa.provider_id, pu.full_name AS provider_name, " +
               "ps.slot_number, ps.vehicle_type " +
               "FROM bookings b " +
               "JOIN users u ON b.user_id = u.user_id " +
               "JOIN parking_areas pa ON b.parking_area_id = pa.parking_area_id " +
               "JOIN users pu ON pa.provider_id = pu.user_id " +
               "JOIN parking_slots ps ON b.slot_id = ps.slot_id ";
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getLong("booking_id"));
        b.setBookingReference(rs.getString("booking_reference"));
        b.setUserId(rs.getLong("user_id"));
        b.setParkingAreaId(rs.getLong("parking_area_id"));
        b.setSlotId(rs.getLong("slot_id"));

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) b.setStartTime(startTs.toLocalDateTime());

        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) b.setEndTime(endTs.toLocalDateTime());

        b.setDurationHours(rs.getBigDecimal("duration_hours"));
        b.setCalculatedFee(rs.getBigDecimal("calculated_fee"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setCancellationReason(rs.getString("cancellation_reason"));

        Timestamp cancelTs = rs.getTimestamp("cancelled_at");
        if (cancelTs != null) b.setCancelledAt(cancelTs.toLocalDateTime());

        b.setCreatedAt(rs.getTimestamp("created_at"));
        b.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            b.setCustomerName(rs.getString("customer_name"));
            b.setCustomerEmail(rs.getString("customer_email"));
            b.setCustomerPhone(rs.getString("customer_phone"));
            b.setParkingName(rs.getString("parking_name"));
            b.setParkingLocation(rs.getString("parking_location"));
            b.setSlotNumber(rs.getString("slot_number"));
            b.setVehicleType(rs.getString("vehicle_type"));
            b.setPricePerHour(rs.getBigDecimal("price_per_hour"));
            b.setProviderId(rs.getLong("provider_id"));
            b.setProviderName(rs.getString("provider_name"));
        } catch (SQLException ignored) {}

        return b;
    }
}
