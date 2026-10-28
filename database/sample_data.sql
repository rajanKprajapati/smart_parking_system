-- ==============================================================================
-- SMART PARKING MANAGEMENT SYSTEM
-- Sample Seed Data for Local Development and Testing
-- Default Password for all test users: Password@123
-- BCrypt Hash: $2a$10$nGG44s3Dft4DWXV6cRVzUOMpJ41QJ/ZKruOVy/jiX3HOsIj/FT0mq
-- ==============================================================================

USE `smart_parking_db`;

-- Clean existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `notifications`;
TRUNCATE TABLE `bookings`;
TRUNCATE TABLE `parking_slots`;
TRUNCATE TABLE `parking_areas`;
TRUNCATE TABLE `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------------------------
-- 1. SEED USERS
-- ------------------------------------------------------------------------------
INSERT INTO `users` (`user_id`, `full_name`, `email`, `password_hash`, `phone_number`, `role`, `account_status`, `created_at`) VALUES
(1, 'John Provider', 'provider@smartparking.com', '$2a$10$nGG44s3Dft4DWXV6cRVzUOMpJ41QJ/ZKruOVy/jiX3HOsIj/FT0mq', '+1-555-0101', 'PROVIDER', 'ACTIVE', NOW() - INTERVAL 30 DAY),
(2, 'Metro Park Corp', 'metro@smartparking.com', '$2a$10$nGG44s3Dft4DWXV6cRVzUOMpJ41QJ/ZKruOVy/jiX3HOsIj/FT0mq', '+1-555-0102', 'PROVIDER', 'ACTIVE', NOW() - INTERVAL 25 DAY),
(3, 'Alice Driver', 'customer@smartparking.com', '$2a$10$nGG44s3Dft4DWXV6cRVzUOMpJ41QJ/ZKruOVy/jiX3HOsIj/FT0mq', '+1-555-0201', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL 20 DAY),
(4, 'Bob Miller', 'bob@smartparking.com', '$2a$10$nGG44s3Dft4DWXV6cRVzUOMpJ41QJ/ZKruOVy/jiX3HOsIj/FT0mq', '+1-555-0202', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL 15 DAY),
(5, 'System Admin', 'admin@smartparking.com', '$2a$10$nGG44s3Dft4DWXV6cRVzUOMpJ41QJ/ZKruOVy/jiX3HOsIj/FT0mq', '+1-555-0999', 'ADMIN', 'ACTIVE', NOW() - INTERVAL 30 DAY);

-- ------------------------------------------------------------------------------
-- 2. SEED PARKING AREAS
-- ------------------------------------------------------------------------------
INSERT INTO `parking_areas` (`parking_area_id`, `provider_id`, `parking_name`, `location`, `description`, `price_per_hour`, `operating_details`, `active_status`, `created_at`) VALUES
(1, 1, 'Downtown Central Plaza', '100 Main Street, Financial District', 'Secure multi-level parking facility with 24/7 CCTV surveillance, automated barrier entry, and EV charging stations.', 5.00, '24/7 Operations', 'ACTIVE', NOW() - INTERVAL 25 DAY),
(2, 1, 'Airport Express Terminal Lot', 'Terminal 2 Boulevard, Airport Corridor', 'Fast shuttle access to airport terminals, covered bays, oversized vehicle accommodations, and luggage assistance.', 8.00, 'Open Daily 04:00 AM - 01:00 AM', 'ACTIVE', NOW() - INTERVAL 20 DAY),
(3, 2, 'Metro Tech Hub Garage', '45 Innovation Way, Silicon Park', 'Modern tech campus parking with smart sensors, dedicated motorcycle zones, and bike storage racks.', 4.00, 'Open Monday-Sunday 06:00 AM - 11:00 PM', 'ACTIVE', NOW() - INTERVAL 18 DAY);

-- ------------------------------------------------------------------------------
-- 3. SEED PARKING SLOTS
-- ------------------------------------------------------------------------------
-- Area 1: Downtown Central Plaza (Provider 1)
INSERT INTO `parking_slots` (`slot_id`, `parking_area_id`, `slot_number`, `vehicle_type`, `active_status`) VALUES
(1, 1, 'CP-A01', 'CAR', 'ACTIVE'),
(2, 1, 'CP-A02', 'CAR', 'ACTIVE'),
(3, 1, 'CP-A03', 'CAR', 'ACTIVE'),
(4, 1, 'CP-B01', 'SUV', 'ACTIVE'),
(5, 1, 'CP-EV01', 'EV', 'ACTIVE'),
(6, 1, 'CP-M01', 'BIKE', 'ACTIVE');

-- Area 2: Airport Express Terminal Lot (Provider 1)
INSERT INTO `parking_slots` (`slot_id`, `parking_area_id`, `slot_number`, `vehicle_type`, `active_status`) VALUES
(7, 2, 'AP-101', 'CAR', 'ACTIVE'),
(8, 2, 'AP-102', 'CAR', 'ACTIVE'),
(9, 2, 'AP-103', 'SUV', 'ACTIVE'),
(10, 2, 'AP-104', 'EV', 'ACTIVE');

-- Area 3: Metro Tech Hub Garage (Provider 2)
INSERT INTO `parking_slots` (`slot_id`, `parking_area_id`, `slot_number`, `vehicle_type`, `active_status`) VALUES
(11, 3, 'TH-01', 'CAR', 'ACTIVE'),
(12, 3, 'TH-02', 'CAR', 'ACTIVE'),
(13, 3, 'TH-03', 'BIKE', 'ACTIVE'),
(14, 3, 'TH-04', 'SUV', 'INACTIVE');

-- ------------------------------------------------------------------------------
-- 4. SEED SAMPLE BOOKINGS
-- ------------------------------------------------------------------------------
-- Booking 1: Past Completed Booking (Customer 3 @ Area 1, Slot 1)
INSERT INTO `bookings` (`booking_id`, `booking_reference`, `user_id`, `parking_area_id`, `slot_id`, `start_time`, `end_time`, `duration_hours`, `calculated_fee`, `booking_status`, `created_at`) VALUES
(1, 'BK-20261020-1001', 3, 1, 1, NOW() - INTERVAL 5 DAY - INTERVAL 4 HOUR, NOW() - INTERVAL 5 DAY, 4.00, 20.00, 'COMPLETED', NOW() - INTERVAL 6 DAY);

-- Booking 2: Cancelled Booking (Customer 4 @ Area 1, Slot 2)
INSERT INTO `bookings` (`booking_id`, `booking_reference`, `user_id`, `parking_area_id`, `slot_id`, `start_time`, `end_time`, `duration_hours`, `calculated_fee`, `booking_status`, `cancellation_reason`, `cancelled_at`, `created_at`) VALUES
(2, 'BK-20261024-1002', 4, 1, 2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 3 HOUR, 3.00, 15.00, 'CANCELLED', 'Travel schedule changed', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 4 DAY);

-- Booking 3: Expired Booking (Customer 3 @ Area 2, Slot 7)
INSERT INTO `bookings` (`booking_id`, `booking_reference`, `user_id`, `parking_area_id`, `slot_id`, `start_time`, `end_time`, `duration_hours`, `calculated_fee`, `booking_status`, `created_at`) VALUES
(3, 'BK-20261028-1003', 3, 2, 7, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 1 HOUR, 5.00, 40.00, 'EXPIRED', NOW() - INTERVAL 1 DAY);

-- Booking 4: Future Upcoming Booking (Customer 3 @ Area 1, Slot 3)
INSERT INTO `bookings` (`booking_id`, `booking_reference`, `user_id`, `parking_area_id`, `slot_id`, `start_time`, `end_time`, `duration_hours`, `calculated_fee`, `booking_status`, `created_at`) VALUES
(4, 'BK-20261030-1004', 3, 1, 3, NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 1 DAY + INTERVAL 2 HOUR, 2.00, 10.00, 'CONFIRMED', NOW() - INTERVAL 2 HOUR);

-- Booking 5: Future Upcoming Booking (Customer 4 @ Area 3, Slot 11)
INSERT INTO `bookings` (`booking_id`, `booking_reference`, `user_id`, `parking_area_id`, `slot_id`, `start_time`, `end_time`, `duration_hours`, `calculated_fee`, `booking_status`, `created_at`) VALUES
(5, 'BK-20261030-1005', 4, 3, 11, NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 4 HOUR, 4.00, 16.00, 'CONFIRMED', NOW() - INTERVAL 1 HOUR);

-- ------------------------------------------------------------------------------
-- 5. SEED NOTIFICATIONS
-- ------------------------------------------------------------------------------
-- Confirmation notifications for Booking 4
INSERT INTO `notifications` (`booking_id`, `recipient_user_id`, `recipient_contact`, `channel`, `notification_type`, `subject`, `message`, `delivery_status`, `attempt_count`, `sent_at`) VALUES
(4, 3, 'customer@smartparking.com', 'EMAIL', 'BOOKING_CONFIRMATION', 'Booking Confirmed - Ref #BK-20261030-1004', 'Hello Alice Driver, your reservation for slot CP-A03 at Downtown Central Plaza is confirmed.', 'SIMULATED', 1, NOW() - INTERVAL 2 HOUR),
(4, 3, '+1-555-0201', 'SMS', 'BOOKING_CONFIRMATION', NULL, 'Booking #BK-20261030-1004 confirmed for CP-A03 at Downtown Central Plaza.', 'SIMULATED', 1, NOW() - INTERVAL 2 HOUR);

-- Expiry notifications for Booking 3 (Customer & Provider)
INSERT INTO `notifications` (`booking_id`, `recipient_user_id`, `recipient_contact`, `channel`, `notification_type`, `subject`, `message`, `delivery_status`, `attempt_count`, `sent_at`) VALUES
(3, 3, 'customer@smartparking.com', 'EMAIL', 'BOOKING_EXPIRY', 'Parking Booking Expired - Ref #BK-20261028-1003', 'Dear Alice Driver, your booking #BK-20261028-1003 has reached its scheduled end time. Please vacate slot AP-101.', 'SIMULATED', 1, NOW() - INTERVAL 1 HOUR),
(3, 3, '+1-555-0201', 'SMS', 'BOOKING_EXPIRY', NULL, 'Booking #BK-20261028-1003 at Airport Express Terminal Lot has expired. Please vacate slot AP-101.', 'SIMULATED', 1, NOW() - INTERVAL 1 HOUR),
(3, 1, 'provider@smartparking.com', 'EMAIL', 'BOOKING_EXPIRY', 'Customer Booking Expired - Ref #BK-20261028-1003', 'Hello John Provider, booking #BK-20261028-1003 for slot AP-101 reached its scheduled end time.', 'SIMULATED', 1, NOW() - INTERVAL 1 HOUR),
(3, 1, '+1-555-0101', 'SMS', 'BOOKING_EXPIRY', NULL, 'Alert: Booking #BK-20261028-1003 for slot AP-101 has expired.', 'SIMULATED', 1, NOW() - INTERVAL 1 HOUR);
