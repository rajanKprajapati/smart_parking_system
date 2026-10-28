package com.smartparking.scheduler;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

/**
 * Servlet Context Listener to manage application startup and graceful shutdown
 * of the background expiry scheduler and thread pools.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppLifecycleListener.class.getName());
    private ExpiryScheduler expiryScheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Smart Parking Management System initializing context...");
        try {
            expiryScheduler = new ExpiryScheduler();
            expiryScheduler.start();
            sce.getServletContext().setAttribute("expiryScheduler", expiryScheduler);
            LOGGER.info("Smart Parking Management System initialization complete.");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize background services: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Smart Parking Management System shutting down context...");
        if (expiryScheduler != null) {
            expiryScheduler.stop();
        }
        LOGGER.info("Smart Parking Management System shutdown complete.");
    }
}
