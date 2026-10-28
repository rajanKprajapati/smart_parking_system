package com.smartparking.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility for formatting and parsing date-time values across controllers, models, and JSP views.
 */
public final class DateTimeUtil {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter ISO_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DateTimeUtil() {
        // Prevent instantiation
    }

    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_FORMATTER);
    }

    public static String formatDisplay(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return formatDisplay(timestamp.toLocalDateTime());
    }

    public static String formatInput(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_INPUT_FORMATTER);
    }

    public static LocalDateTime parseInput(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(input.trim(), ISO_INPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(input.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public static LocalDateTime parseDateAndTime(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return null;
        String combined = dateStr.trim() + "T" + timeStr.trim();
        return parseInput(combined);
    }

    public static Timestamp toTimestamp(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Timestamp.valueOf(ldt);
    }

    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime();
    }
}
