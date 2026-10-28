package com.smartparking.service;

import com.smartparking.dao.UserDao;
import com.smartparking.model.User;
import com.smartparking.util.PasswordUtil;
import com.smartparking.util.ValidationUtil;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing user registration, authentication, and profile updates.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(String email, String plainPassword) {
        if (!ValidationUtil.isValidEmail(email) || !ValidationUtil.isNotEmpty(plainPassword)) {
            return null;
        }

        User user = userDao.findByEmail(email);
        if (user == null || !user.isActive()) {
            return null;
        }

        if (PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    public String register(String fullName, String email, String plainPassword, String phone, String roleStr) {
        if (!ValidationUtil.isNotEmpty(fullName)) {
            return "Full name is required.";
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return "A valid email address is required.";
        }
        if (!ValidationUtil.isNotEmpty(plainPassword) || plainPassword.length() < 6) {
            return "Password must be at least 6 characters in length.";
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            return "A valid phone number (7-20 digits) is required.";
        }

        User.Role role = User.Role.CUSTOMER;
        if ("PROVIDER".equalsIgnoreCase(roleStr)) {
            role = User.Role.PROVIDER;
        }

        if (userDao.existsByEmail(email)) {
            return "An account with email " + email + " already exists.";
        }

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User user = new User(null, fullName.trim(), email.trim().toLowerCase(), hashedPassword, phone.trim(), role);

        try {
            Long userId = userDao.createUser(user);
            if (userId != null) {
                return null; // Success, no error
            }
            return "Failed to register account. Please try again.";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during registration", e);
            return "Database error during registration. Please try again later.";
        }
    }

    public boolean updateProfile(Long userId, String fullName, String phone) {
        if (userId == null || !ValidationUtil.isNotEmpty(fullName) || !ValidationUtil.isValidPhone(phone)) {
            return false;
        }
        User user = new User();
        user.setUserId(userId);
        user.setFullName(fullName.trim());
        user.setPhoneNumber(phone.trim());
        try {
            return userDao.updateProfile(user);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user profile", e);
            return false;
        }
    }

    public String changePassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null || !ValidationUtil.isNotEmpty(currentPassword) || !ValidationUtil.isNotEmpty(newPassword)) {
            return "All password fields are required.";
        }
        if (newPassword.length() < 6) {
            return "New password must be at least 6 characters.";
        }

        User user = userDao.findById(userId);
        if (user == null) {
            return "User not found.";
        }

        if (!PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) {
            return "Current password does not match our records.";
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        try {
            boolean updated = userDao.updatePassword(userId, newHash);
            return updated ? null : "Failed to update password.";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error changing password", e);
            return "Database error while changing password.";
        }
    }
}
