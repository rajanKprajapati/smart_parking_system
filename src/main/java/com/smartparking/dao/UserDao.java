package com.smartparking.dao;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for users table.
 */
public class UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    public Long createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone_number, role, account_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getAccountStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        user.setUserId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + user.getEmail(), e);
            throw e;
        }
        return null;
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT user_id, full_name, email, password_hash, phone_number, role, account_status, created_at, updated_at " +
                     "FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding user by email: " + email, e);
        }
        return null;
    }

    public User findById(Long userId) {
        if (userId == null) return null;
        String sql = "SELECT user_id, full_name, email, password_hash, phone_number, role, account_status, created_at, updated_at " +
                     "FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding user by ID: " + userId, e);
        }
        return null;
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking email existence: " + email, e);
            return false;
        }
    }

    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone_number = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhoneNumber());
            ps.setLong(3, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user profile for ID: " + user.getUserId(), e);
            throw e;
        }
    }

    public boolean updatePassword(Long userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for ID: " + userId, e);
            throw e;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(rs.getString("role"));
        user.setAccountStatus(rs.getString("account_status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
