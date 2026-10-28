package com.smartparking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe JDBC Connection provider for the Smart Parking application.
 * Manages JDBC Driver loading and establishes connections to MySQL.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static boolean driverLoaded = false;

    static {
        try {
            Class.forName(DRIVER_CLASS);
            driverLoaded = true;
            LOGGER.info("MySQL JDBC Driver registered successfully: " + DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Could not find MySQL JDBC Driver class: " + DRIVER_CLASS, e);
        }
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }

    /**
     * Obtains a new active connection to the configured MySQL database.
     *
     * @return an open {@link Connection}
     * @throws SQLException if a database access error occurs or credentials are invalid
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName(DRIVER_CLASS);
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver is missing from application classpath.", e);
            }
        }

        String url = AppConfig.getJdbcUrl();
        String user = AppConfig.getDbUser();
        String password = AppConfig.getDbPassword();

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to connect to MySQL database at " + url + " as user " + user + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Tests whether the database connection is currently operational.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}
