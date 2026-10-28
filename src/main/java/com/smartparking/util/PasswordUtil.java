package com.smartparking.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing and verification using standard BCrypt.
 */
public final class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 10;

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hashes a plain-text password using BCrypt with a secure salt.
     *
     * @param plainPassword the plain text password
     * @return the BCrypt-hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword  the candidate password
     * @param hashedPassword the stored BCrypt hash
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String testPass = args.length > 0 ? args[0] : "Password@123";
        String hash = hashPassword(testPass);
        System.out.println("PASSWORD_TEST: " + testPass);
        System.out.println("HASH_TEST: " + hash);
        System.out.println("VERIFY_TEST: " + checkPassword(testPass, hash));
    }
}
