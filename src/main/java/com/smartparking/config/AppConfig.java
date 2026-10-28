package com.smartparking.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central application configuration manager.
 * Loads configuration from Environment Variables, System Properties,
 * or class-path resource 'db.properties'.
 */
public final class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private AppConfig() {
        // Prevent instantiation
    }

    private static void loadProperties() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
                LOGGER.info("Successfully loaded db.properties from classpath.");
            } else {
                LOGGER.warning("db.properties not found in classpath. Relying on environment variables or defaults.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load db.properties", e);
        }
    }

    public static String get(String key, String defaultValue) {
        // 1. Check System Environment (converted e.g. db.host -> DB_HOST)
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        // 2. Check System property
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.trim().isEmpty()) {
            return sysVal.trim();
        }

        // 3. Check loaded properties file
        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid integer value for key " + key + ": " + val);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    // Database accessors
    public static String getDbHost() {
        return get("db.host", "localhost");
    }

    public static int getDbPort() {
        return getInt("db.port", 3306);
    }

    public static String getDbName() {
        return get("db.name", "smart_parking_db");
    }

    public static String getDbUser() {
        return get("db.user", "root");
    }

    public static String getDbPassword() {
        return get("db.password", "root");
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getDbHost() + ":" + getDbPort() + "/" + getDbName()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    // Application state
    public static boolean isDevelopmentMode() {
        String env = get("app.env", "development");
        return "development".equalsIgnoreCase(env) || "dev".equalsIgnoreCase(env);
    }

    public static boolean isDebugEnabled() {
        return getBoolean("app.debug", true);
    }

    public static String getAppBaseUrl() {
        return get("app.baseUrl", "http://localhost:8080/smart-parking");
    }

    // Mail
    public static boolean isMailEnabled() {
        return getBoolean("mail.enabled", false);
    }

    public static String getMailSmtpHost() {
        return get("mail.smtp.host", "smtp.gmail.com");
    }

    public static int getMailSmtpPort() {
        return getInt("mail.smtp.port", 587);
    }

    public static boolean isMailSmtpAuth() {
        return getBoolean("mail.smtp.auth", true);
    }

    public static boolean isMailSmtpStarttls() {
        return getBoolean("mail.smtp.starttls", true);
    }

    public static String getMailUsername() {
        return get("mail.username", "");
    }

    public static String getMailPassword() {
        return get("mail.password", "");
    }

    public static String getMailFrom() {
        return get("mail.from", "no-reply@smartparking.local");
    }

    public static String getMailFromName() {
        return get("mail.fromName", "Smart Parking System");
    }

    // SMS
    public static boolean isSmsEnabled() {
        return getBoolean("sms.enabled", false);
    }

    public static String getTwilioAccountSid() {
        return get("sms.twilio.accountSid", "");
    }

    public static String getTwilioAuthToken() {
        return get("sms.twilio.authToken", "");
    }

    public static String getTwilioFromNumber() {
        return get("sms.twilio.fromNumber", "");
    }

    // Scheduler
    public static boolean isSchedulerEnabled() {
        return getBoolean("scheduler.enabled", true);
    }

    public static int getSchedulerIntervalSeconds() {
        return getInt("scheduler.intervalSeconds", 60);
    }

    public static int getNotificationMaxRetries() {
        return getInt("notification.maxRetries", 3);
    }
}
