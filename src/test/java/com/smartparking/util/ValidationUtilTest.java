package com.smartparking.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    @DisplayName("Email validation: accepts standard emails and rejects malformed")
    void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("driver@example.com"));
        assertTrue(ValidationUtil.isValidEmail("john.doe+parking@sub.domain.org"));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@nodomain.com"));
        assertFalse(ValidationUtil.isValidEmail("missing@tld"));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
    }

    @Test
    @DisplayName("Phone validation: accepts standard international and domestic formats")
    void testPhoneValidation() {
        assertTrue(ValidationUtil.isValidPhone("+1-555-0199"));
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("+91 98765 43210"));
        assertFalse(ValidationUtil.isValidPhone("123")); // too short
        assertFalse(ValidationUtil.isValidPhone("abc-def-ghij"));
        assertFalse(ValidationUtil.isValidPhone(null));
    }

    @Test
    @DisplayName("Interval validation: rejects past start dates")
    void testPastInterval() {
        LocalDateTime past = LocalDateTime.now().minusHours(3);
        LocalDateTime future = LocalDateTime.now().plusHours(2);

        String err = ValidationUtil.validateBookingInterval(past, future);
        assertNotNull(err);
        assertTrue(err.contains("past"));
    }

    @Test
    @DisplayName("Interval validation: rejects end before start")
    void testEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusHours(3);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        String err = ValidationUtil.validateBookingInterval(start, end);
        assertNotNull(err);
        assertTrue(err.contains("later than start time"));
    }

    @Test
    @DisplayName("Interval validation: accepts valid future intervals")
    void testValidInterval() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);

        String err = ValidationUtil.validateBookingInterval(start, end);
        assertNull(err);
    }
}
