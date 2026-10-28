package com.smartparking.util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Common input validation utilities for user input, formats, and booking time intervals.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\-\\s()]{7,20}$");

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 150 && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        return !trimmed.isEmpty() && PHONE_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates a parking booking interval.
     *
     * @param start start time
     * @param end   end time
     * @return null if valid, or a descriptive error message if invalid
     */
    public static String validateBookingInterval(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return "Start time is required.";
        }
        if (end == null) {
            return "End time is required.";
        }
        LocalDateTime now = LocalDateTime.now().minusMinutes(2); // allow small 2-minute clock skew for submission latency
        if (start.isBefore(now)) {
            return "Booking start time cannot be in the past.";
        }
        if (!end.isAfter(start)) {
            return "Booking end time must be later than start time.";
        }
        long minutes = java.time.Duration.between(start, end).toMinutes();
        if (minutes < 30) {
            return "Minimum booking duration is 30 minutes.";
        }
        if (minutes > 30 * 24 * 60) {
            return "Maximum booking duration cannot exceed 30 days.";
        }
        return null; // Valid
    }
}
