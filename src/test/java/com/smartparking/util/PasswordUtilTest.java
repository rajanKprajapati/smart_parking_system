package com.smartparking.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing: generates valid BCrypt hash and verifies successfully")
    void testHashingAndVerification() {
        String rawPass = "SecurePass@2026";
        String hash = PasswordUtil.hashPassword(rawPass);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(PasswordUtil.checkPassword(rawPass, hash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword123", hash));
    }

    @Test
    @DisplayName("Password verification: handles null and empty inputs safely without exceptions")
    void testNullSafe() {
        assertFalse(PasswordUtil.checkPassword(null, "someHash"));
        assertFalse(PasswordUtil.checkPassword("pass", null));
        assertFalse(PasswordUtil.checkPassword("", ""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
    }
}
