package com.smartparking.notification;

import com.smartparking.config.AppConfig;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for dispatching SMS notifications via Twilio REST API,
 * with transparent support for development simulation mode.
 */
public class SmsService {

    private static final Logger LOGGER = Logger.getLogger(SmsService.class.getName());

    public SmsService() {
        // No eager socket allocation in constructor to maintain lightweight initialization
    }

    public DispatchResult sendSms(String toPhoneNumber, String messageBody) {
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            return DispatchResult.failed("Recipient phone number is missing or blank");
        }

        // If SMS is disabled or credentials are blank -> Development Simulation Mode
        if (!AppConfig.isSmsEnabled() || AppConfig.getTwilioAccountSid().isEmpty() || AppConfig.getTwilioAuthToken().isEmpty()) {
            LOGGER.info("[SIMULATED SMS] To: " + toPhoneNumber + " | Message: " + messageBody);
            return DispatchResult.simulated();
        }

        // Real Twilio REST API dispatch via standard HTTP connection
        try {
            String sid = AppConfig.getTwilioAccountSid();
            String token = AppConfig.getTwilioAuthToken();
            String from = AppConfig.getTwilioFromNumber();

            URL url = new URL("https://api.twilio.com/2010-04-01/Accounts/" + sid + "/Messages.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            String auth = sid + ":" + token;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            String formPayload = "To=" + URLEncoder.encode(toPhoneNumber.trim(), StandardCharsets.UTF_8) +
                                 "&From=" + URLEncoder.encode(from.trim(), StandardCharsets.UTF_8) +
                                 "&Body=" + URLEncoder.encode(messageBody, StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(formPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                LOGGER.info("Twilio SMS sent successfully to: " + toPhoneNumber);
                return DispatchResult.sent();
            } else {
                String error = "Twilio API HTTP " + responseCode + ": " + conn.getResponseMessage();
                LOGGER.warning("Twilio SMS failed: " + error);
                return DispatchResult.failed(error.length() > 250 ? error.substring(0, 250) : error);
            }
        } catch (Exception e) {
            String error = "SMS dispatch error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            LOGGER.log(Level.WARNING, "Failed to send SMS to " + toPhoneNumber, e);
            return DispatchResult.failed(error.length() > 250 ? error.substring(0, 250) : error);
        }
    }
}
