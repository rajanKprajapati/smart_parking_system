package com.smartparking.service;

import com.smartparking.config.DatabaseConnection;
import com.smartparking.dao.BookingDao;
import com.smartparking.dao.ParkingAreaDao;
import com.smartparking.dao.ParkingSlotDao;
import com.smartparking.model.Booking;
import com.smartparking.model.DashboardStats;
import com.smartparking.model.ParkingArea;
import com.smartparking.model.ParkingSlot;
import com.smartparking.util.FeeCalculator;
import com.smartparking.util.ValidationUtil;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing parking reservations, transactional concurrency control,
 * server-side fee calculations, and cancellation workflows.
 */
public class BookingService {

    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingDao bookingDao;
    private final ParkingAreaDao parkingAreaDao;
    private final ParkingSlotDao parkingSlotDao;
    private final NotificationService notificationService;

    public BookingService() {
        this.bookingDao = new BookingDao();
        this.parkingAreaDao = new ParkingAreaDao();
        this.parkingSlotDao = new ParkingSlotDao();
        this.notificationService = new NotificationService();
    }

    public BookingService(BookingDao bookingDao, ParkingAreaDao parkingAreaDao,
                          ParkingSlotDao parkingSlotDao, NotificationService notificationService) {
        this.bookingDao = bookingDao;
        this.parkingAreaDao = parkingAreaDao;
        this.parkingSlotDao = parkingSlotDao;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new parking reservation within an atomic database transaction.
     * Guarantees overlap prevention, server-side fee calculation, and notification dispatch.
     *
     * @param userId customer user ID
     * @param areaId parking area ID
     * @param slotId parking slot ID
     * @param start  start date-time
     * @param end    end date-time
     * @return the created {@link Booking} on success
     * @throws IllegalArgumentException on validation or availability conflict
     * @throws SQLException             on database error
     */
    public Booking createBooking(Long userId, Long areaId, Long slotId, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        return createBooking(userId, areaId, slotId, start, end, Booking.PaymentStatus.PENDING, "UPI_QR", null);
    }

    /**
     * Creates a new parking reservation within an atomic database transaction.
     * Guarantees overlap prevention, server-side fee calculation, payment state capture, and notification dispatch.
     */
    public Booking createBooking(Long userId, Long areaId, Long slotId, LocalDateTime start, LocalDateTime end,
                                 Booking.PaymentStatus paymentStatus, String paymentMethod, String paymentReference)
            throws SQLException {

        // 1. Validate Interval
        String intervalError = ValidationUtil.validateBookingInterval(start, end);
        if (intervalError != null) {
            throw new IllegalArgumentException(intervalError);
        }

        // 2. Validate Area and Slot existence and active status
        ParkingArea area = parkingAreaDao.findById(areaId);
        if (area == null || !area.isActive()) {
            throw new IllegalArgumentException("Selected parking area is inactive or unavailable.");
        }

        ParkingSlot slot = parkingSlotDao.findById(slotId);
        if (slot == null || !slot.isActive() || !slot.getParkingAreaId().equals(areaId)) {
            throw new IllegalArgumentException("Selected parking slot is invalid, inactive, or belongs to another area.");
        }

        // 3. Calculate Fee on backend (never trust client)
        BigDecimal durationHours = FeeCalculator.calculateDurationHours(start, end);
        BigDecimal calculatedFee = FeeCalculator.calculateFee(start, end, area.getPricePerHour());
        String bookingReference = generateBookingReference();

        Booking booking = new Booking();
        booking.setBookingReference(bookingReference);
        booking.setUserId(userId);
        booking.setParkingAreaId(areaId);
        booking.setSlotId(slotId);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setDurationHours(durationHours);
        booking.setCalculatedFee(calculatedFee);
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(paymentStatus != null ? paymentStatus : Booking.PaymentStatus.PENDING);
        booking.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod.trim() : "UPI_QR");
        booking.setPaymentReference(paymentReference != null && !paymentReference.trim().isEmpty() ? paymentReference.trim() : null);
        if (Booking.PaymentStatus.PAID.equals(paymentStatus)) {
            booking.setPaidAt(LocalDateTime.now());
        }

        // 4. Atomic Transaction with Concurrency Lock
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Re-verify availability inside transaction with row-level lock
                boolean available = parkingSlotDao.isSlotAvailableInInterval(slotId, start, end, null, conn);
                if (!available) {
                    conn.rollback();
                    throw new IllegalArgumentException("Slot " + slot.getSlotNumber() + " was just reserved by another customer for this time window. Please select another slot or time.");
                }

                Long bookingId = bookingDao.createBooking(booking, conn);
                if (bookingId == null) {
                    conn.rollback();
                    throw new SQLException("Failed to save booking record to database.");
                }

                conn.commit();
                LOGGER.info("Booking #" + bookingReference + " (ID: " + bookingId + ") successfully created for user: " + userId);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }

        // 5. Populate display data and dispatch confirmation notifications
        Booking saved = bookingDao.findById(booking.getBookingId());
        if (saved != null) {
            try {
                notificationService.sendBookingConfirmation(saved);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to send booking confirmation notifications", ex);
            }
            return saved;
        }

        return booking;
    }

    /**
     * Cancels an eligible booking, immediately freeing the reserved time interval.
     */
    public boolean cancelBooking(Long bookingId, Long userId, String reason) throws SQLException {
        if (bookingId == null || userId == null) return false;

        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found.");
        }

        // Ownership check
        if (!userId.equals(booking.getUserId())) {
            throw new IllegalArgumentException("Access denied: You are not authorized to cancel this reservation.");
        }

        // Eligibility check
        if (!booking.isEligibleForCancellation()) {
            throw new IllegalArgumentException("Only upcoming confirmed reservations can be cancelled. Active or past bookings cannot be cancelled.");
        }

        boolean cancelled = bookingDao.cancelBooking(bookingId, userId, reason, null);
        if (cancelled) {
            LOGGER.info("Booking #" + booking.getBookingReference() + " successfully cancelled by user: " + userId);
            try {
                notificationService.sendBookingCancellation(booking, reason);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error dispatching cancellation notification", ex);
            }
        }
        return cancelled;
    }

    public List<Booking> getCustomerBookings(Long userId, String statusFilter) {
        if (userId == null) return Collections.emptyList();
        return bookingDao.findByUserId(userId, statusFilter);
    }

    public List<Booking> getProviderBookings(Long providerId, String statusFilter) {
        if (providerId == null) return Collections.emptyList();
        return bookingDao.findByProviderId(providerId, statusFilter);
    }

    public Booking getBookingDetails(Long bookingId, Long requestingUserId, boolean isProvider) {
        if (bookingId == null) return null;
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) return null;

        // Security check: Customer can only view their own; Provider can only view bookings for their facilities
        if (!isProvider && !requestingUserId.equals(booking.getUserId())) {
            return null;
        }
        if (isProvider && !requestingUserId.equals(booking.getProviderId())) {
            return null;
        }

        return booking;
    }

    public DashboardStats getProviderDashboardStats(Long providerId) {
        return bookingDao.getProviderDashboardStats(providerId);
    }

    /**
     * Searches customer bookings for a specific provider by customer name, email, phone, or booking ID.
     * Enforces provider isolation so providers only see bookings from their own facilities.
     */
    public List<Booking> searchProviderCustomers(Long providerId, String keyword) {
        if (providerId == null || keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return bookingDao.searchProviderBookings(providerId, keyword);
    }

    /**
     * Updates payment status (e.g. manual verification workflow) with provider ownership verification.
     */
    public boolean updatePaymentStatus(Long bookingId, Long providerId, Booking.PaymentStatus newStatus, String reference) {
        if (bookingId == null || newStatus == null) return false;
        boolean updated = bookingDao.updatePaymentStatus(bookingId, providerId, newStatus, reference);
        if (updated) {
            LOGGER.info("Payment status for booking ID " + bookingId + " updated to " + newStatus + " by provider " + providerId);
        }
        return updated;
    }

    private String generateBookingReference() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int randNum = 1000 + RANDOM.nextInt(9000);
        return "BK-" + dateStr + "-" + randNum;
    }
}
