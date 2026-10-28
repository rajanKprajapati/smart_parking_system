package com.smartparking.scheduler;

import com.smartparking.config.AppConfig;
import com.smartparking.config.DatabaseConnection;
import com.smartparking.dao.BookingDao;
import com.smartparking.model.Booking;
import com.smartparking.service.NotificationService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background scheduler for automatic parking booking expiry processing.
 * Scans periodically for confirmed bookings whose end time has elapsed,
 * updates their status to EXPIRED, and triggers email and SMS alerts for
 * both customers and parking providers.
 */
public class ExpiryScheduler {

    private static final Logger LOGGER = Logger.getLogger(ExpiryScheduler.class.getName());

    private final ScheduledExecutorService scheduler;
    private final BookingDao bookingDao;
    private final NotificationService notificationService;
    private boolean running = false;

    public ExpiryScheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SmartParking-ExpiryScheduler");
            t.setDaemon(true);
            return t;
        });
        this.bookingDao = new BookingDao();
        this.notificationService = new NotificationService();
    }

    public synchronized void start() {
        if (!AppConfig.isSchedulerEnabled()) {
            LOGGER.info("Expiry background scheduler is disabled by configuration.");
            return;
        }

        if (running) {
            LOGGER.warning("ExpiryScheduler is already running.");
            return;
        }

        int intervalSec = AppConfig.getSchedulerIntervalSeconds();
        LOGGER.info("Starting ExpiryScheduler background task (runs every " + intervalSec + " seconds)...");

        scheduler.scheduleWithFixedDelay(this::processExpiredBookings, 15, intervalSec, TimeUnit.SECONDS);
        running = true;
    }

    public synchronized void stop() {
        if (!running) return;
        LOGGER.info("Stopping ExpiryScheduler background task...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        running = false;
        LOGGER.info("ExpiryScheduler successfully stopped.");
    }

    /**
     * Identifies and processes bookings whose scheduled end time has elapsed.
     */
    public void processExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Find all bookings with status=CONFIRMED and end_time <= now
                List<Booking> expiredList = bookingDao.findExpiredEligibleBookings(now, conn);

                if (!expiredList.isEmpty()) {
                    LOGGER.info("Found " + expiredList.size() + " expired booking(s) eligible for automated processing.");

                    for (Booking b : expiredList) {
                        // 1. Transition status from CONFIRMED -> EXPIRED
                        bookingDao.updateStatus(b.getBookingId(), Booking.BookingStatus.EXPIRED, conn);

                        // 2. Queue expiry notifications (Customer Email, Customer SMS, Provider Email, Provider SMS)
                        notificationService.sendBookingExpiry(b, conn);
                    }

                    conn.commit();
                    LOGGER.info("Successfully updated " + expiredList.size() + " booking(s) to EXPIRED and dispatched notifications.");
                } else {
                    conn.commit();
                }
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Transaction rollback during expiry processing", e);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database connection error in ExpiryScheduler run: " + e.getMessage());
        }
    }
}
