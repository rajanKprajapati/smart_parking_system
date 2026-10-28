-- ==============================================================================
-- SMART PARKING MANAGEMENT SYSTEM
-- Database Schema Definition (MySQL 8.0+)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `smart_parking_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `smart_parking_db`;

-- Drop tables in dependency order if they already exist
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `parking_slots`;
DROP TABLE IF EXISTS `parking_areas`;
DROP TABLE IF EXISTS `users`;

-- ------------------------------------------------------------------------------
-- 1. USERS TABLE
-- Stores credentials, personal details, roles (CUSTOMER, PROVIDER, ADMIN),
-- and account status.
-- ------------------------------------------------------------------------------
CREATE TABLE `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `phone_number` VARCHAR(20) NOT NULL,
  `role` ENUM('CUSTOMER', 'PROVIDER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  `account_status` ENUM('ACTIVE', 'SUSPENDED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_phone` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. PARKING AREAS TABLE
-- Managed by parking providers. Contains name, location, hourly rate,
-- operating details, and active status.
-- ------------------------------------------------------------------------------
CREATE TABLE `parking_areas` (
  `parking_area_id` BIGINT NOT NULL AUTO_INCREMENT,
  `provider_id` BIGINT NOT NULL,
  `parking_name` VARCHAR(150) NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `price_per_hour` DECIMAL(10,2) NOT NULL,
  `operating_details` VARCHAR(255) NOT NULL DEFAULT '24/7 Operations',
  `active_status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`parking_area_id`),
  KEY `idx_parking_areas_provider` (`provider_id`),
  KEY `idx_parking_areas_active` (`active_status`),
  KEY `idx_parking_areas_location` (`location`),
  KEY `idx_parking_areas_price` (`price_per_hour`),
  CONSTRAINT `fk_parking_areas_provider`
    FOREIGN KEY (`provider_id`) REFERENCES `users` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. PARKING SLOTS TABLE
-- Individual physical parking bays under each parking area.
-- Identified by unique slot_number per area.
-- ------------------------------------------------------------------------------
CREATE TABLE `parking_slots` (
  `slot_id` BIGINT NOT NULL AUTO_INCREMENT,
  `parking_area_id` BIGINT NOT NULL,
  `slot_number` VARCHAR(50) NOT NULL,
  `vehicle_type` ENUM('CAR', 'BIKE', 'SUV', 'EV', 'ANY') NOT NULL DEFAULT 'CAR',
  `active_status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`slot_id`),
  UNIQUE KEY `uk_area_slot_number` (`parking_area_id`, `slot_number`),
  KEY `idx_slots_area` (`parking_area_id`),
  KEY `idx_slots_status` (`active_status`),
  KEY `idx_slots_type` (`vehicle_type`),
  CONSTRAINT `fk_slots_parking_area`
    FOREIGN KEY (`parking_area_id`) REFERENCES `parking_areas` (`parking_area_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. BOOKINGS TABLE
-- Customer reservations for slots within a specific time interval.
-- Availability is determined dynamically by overlapping CONFIRMED reservations.
-- ------------------------------------------------------------------------------
CREATE TABLE `bookings` (
  `booking_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_reference` VARCHAR(50) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `parking_area_id` BIGINT NOT NULL,
  `slot_id` BIGINT NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `duration_hours` DECIMAL(5,2) NOT NULL,
  `calculated_fee` DECIMAL(10,2) NOT NULL,
  `booking_status` ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED', 'EXPIRED') NOT NULL DEFAULT 'CONFIRMED',
  `cancellation_reason` VARCHAR(255) NULL,
  `cancelled_at` DATETIME NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `uk_booking_reference` (`booking_reference`),
  KEY `idx_bookings_user` (`user_id`),
  KEY `idx_bookings_area` (`parking_area_id`),
  KEY `idx_bookings_slot_interval` (`slot_id`, `start_time`, `end_time`, `booking_status`),
  KEY `idx_bookings_status_endtime` (`booking_status`, `end_time`),
  CONSTRAINT `fk_bookings_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bookings_area`
    FOREIGN KEY (`parking_area_id`) REFERENCES `parking_areas` (`parking_area_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bookings_slot`
    FOREIGN KEY (`slot_id`) REFERENCES `parking_slots` (`slot_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 5. NOTIFICATIONS TABLE
-- Records email and SMS dispatches for confirmations, expiries, cancellations.
-- Distinct delivery statuses: PENDING, SENT, SIMULATED, FAILED, RETRYING.
-- Database-backed idempotency prevents duplicate notifications.
-- ------------------------------------------------------------------------------
CREATE TABLE `notifications` (
  `notification_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `recipient_user_id` BIGINT NOT NULL,
  `recipient_contact` VARCHAR(150) NOT NULL,
  `channel` ENUM('EMAIL', 'SMS') NOT NULL,
  `notification_type` ENUM('BOOKING_CONFIRMATION', 'BOOKING_EXPIRY', 'BOOKING_CANCELLED') NOT NULL,
  `subject` VARCHAR(200) NULL,
  `message` TEXT NOT NULL,
  `delivery_status` ENUM('PENDING', 'SENT', 'SIMULATED', 'FAILED', 'RETRYING') NOT NULL DEFAULT 'PENDING',
  `attempt_count` INT NOT NULL DEFAULT 0,
  `max_retries` INT NOT NULL DEFAULT 3,
  `last_attempt_at` DATETIME NULL,
  `sent_at` DATETIME NULL,
  `safe_error_summary` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  UNIQUE KEY `uk_notification_idempotency` (`booking_id`, `recipient_user_id`, `channel`, `notification_type`),
  KEY `idx_notifications_user` (`recipient_user_id`),
  KEY `idx_notifications_booking` (`booking_id`),
  KEY `idx_notifications_status` (`delivery_status`),
  CONSTRAINT `fk_notifications_booking`
    FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_notifications_user`
    FOREIGN KEY (`recipient_user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
