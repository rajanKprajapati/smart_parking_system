package com.smartparking.dao;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.model.Notification;
import com.smartparking.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for notifications table.
 */
public class NotificationDao {

    private static final Logger LOGGER = Logger.getLogger(NotificationDao.class.getName());

    /**
     * Creates a notification record. Uses INSERT IGNORE so identical notification attempts
     * (matching uk_notification_idempotency) return null without failing the calling transaction.
     */
    public Long createNotification(Notification n, Connection externalConn) throws SQLException {
        String sql = "INSERT IGNORE INTO notifications (booking_id, recipient_user_id, recipient_contact, " +
                     "channel, notification_type, subject, message, delivery_status, attempt_count, max_retries, " +
                     "last_attempt_at, sent_at, safe_error_summary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = (externalConn != null) ? externalConn : DatabaseConnection.getConnection();
        boolean closeConn = (externalConn == null);

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, n.getBookingId());
            ps.setLong(2, n.getRecipientUserId());
            ps.setString(3, n.getRecipientContact());
            ps.setString(4, n.getChannel().name());
            ps.setString(5, n.getNotificationType().name());
            ps.setString(6, n.getSubject());
            ps.setString(7, n.getMessage());
            ps.setString(8, n.getDeliveryStatus().name());
            ps.setInt(9, n.getAttemptCount());
            ps.setInt(10, n.getMaxRetries());
            ps.setTimestamp(11, DateTimeUtil.toTimestamp(n.getLastAttemptAt()));
            ps.setTimestamp(12, DateTimeUtil.toTimestamp(n.getSentAt()));
            ps.setString(13, n.getSafeErrorSummary());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        n.setNotificationId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting notification for booking: " + n.getBookingId(), e);
            throw e;
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
        return null;
    }

    public boolean updateStatus(Long notificationId, Notification.DeliveryStatus status,
                                int attemptCount, String safeErrorSummary, Timestamp sentAt) {
        String sql = "UPDATE notifications SET delivery_status = ?, attempt_count = ?, " +
                     "last_attempt_at = NOW(), sent_at = ?, safe_error_summary = ? " +
                     "WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, attemptCount);
            ps.setTimestamp(3, sentAt);
            ps.setString(4, safeErrorSummary);
            ps.setLong(5, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating notification status for ID: " + notificationId, e);
            return false;
        }
    }

    public Notification findById(Long notificationId) {
        if (notificationId == null) return null;
        String sql = baseNotificationSelect() + "WHERE n.notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding notification by ID: " + notificationId, e);
        }
        return null;
    }

    public List<Notification> findByUserId(Long userId) {
        List<Notification> list = new ArrayList<>();
        if (userId == null) return list;
        String sql = baseNotificationSelect() + "WHERE n.recipient_user_id = ? ORDER BY n.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding notifications for user ID: " + userId, e);
        }
        return list;
    }

    public List<Notification> findByBookingId(Long bookingId) {
        List<Notification> list = new ArrayList<>();
        if (bookingId == null) return list;
        String sql = baseNotificationSelect() + "WHERE n.booking_id = ? ORDER BY n.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding notifications for booking ID: " + bookingId, e);
        }
        return list;
    }

    public List<Notification> findFailedEligibleNotifications(int maxRetries) {
        List<Notification> list = new ArrayList<>();
        String sql = baseNotificationSelect() +
                     "WHERE n.delivery_status IN ('FAILED', 'RETRYING') AND n.attempt_count < ? " +
                     "ORDER BY n.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maxRetries);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding failed eligible notifications", e);
        }
        return list;
    }

    private String baseNotificationSelect() {
        return "SELECT n.*, " +
               "b.booking_reference, pa.parking_name, ps.slot_number, u.full_name AS recipient_name " +
               "FROM notifications n " +
               "JOIN bookings b ON n.booking_id = b.booking_id " +
               "JOIN parking_areas pa ON b.parking_area_id = pa.parking_area_id " +
               "JOIN parking_slots ps ON b.slot_id = ps.slot_id " +
               "JOIN users u ON n.recipient_user_id = u.user_id ";
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getLong("notification_id"));
        n.setBookingId(rs.getLong("booking_id"));
        n.setRecipientUserId(rs.getLong("recipient_user_id"));
        n.setRecipientContact(rs.getString("recipient_contact"));
        n.setChannel(rs.getString("channel"));
        n.setNotificationType(rs.getString("notification_type"));
        n.setSubject(rs.getString("subject"));
        n.setMessage(rs.getString("message"));
        n.setDeliveryStatus(rs.getString("delivery_status"));
        n.setAttemptCount(rs.getInt("attempt_count"));
        n.setMaxRetries(rs.getInt("max_retries"));

        Timestamp lastAttemptTs = rs.getTimestamp("last_attempt_at");
        if (lastAttemptTs != null) n.setLastAttemptAt(lastAttemptTs.toLocalDateTime());

        Timestamp sentTs = rs.getTimestamp("sent_at");
        if (sentTs != null) n.setSentAt(sentTs.toLocalDateTime());

        n.setSafeErrorSummary(rs.getString("safe_error_summary"));
        n.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            n.setBookingReference(rs.getString("booking_reference"));
            n.setParkingName(rs.getString("parking_name"));
            n.setSlotNumber(rs.getString("slot_number"));
            n.setRecipientName(rs.getString("recipient_name"));
        } catch (SQLException ignored) {}

        return n;
    }
}
