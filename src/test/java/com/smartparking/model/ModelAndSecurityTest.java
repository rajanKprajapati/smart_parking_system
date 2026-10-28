package com.smartparking.model;

import com.smartparking.util.CsrfTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndSecurityTest {

    @Test
    @DisplayName("User Role checking: correctly identifies Customer, Provider, and Admin roles")
    void testUserRoles() {
        User customer = new User(1L, "Alice Customer", "alice@test.com", "hash", "+1555", User.Role.CUSTOMER);
        assertTrue(customer.isCustomer());
        assertFalse(customer.isProvider());
        assertFalse(customer.isAdmin());

        User provider = new User(2L, "Bob Owner", "bob@test.com", "hash", "+1555", User.Role.PROVIDER);
        assertFalse(provider.isCustomer());
        assertTrue(provider.isProvider());
        assertFalse(provider.isAdmin());

        User admin = new User(3L, "System Admin", "admin@test.com", "hash", "+1555", User.Role.ADMIN);
        assertFalse(admin.isCustomer());
        assertFalse(admin.isProvider());
        assertTrue(admin.isAdmin());
    }

    @Test
    @DisplayName("Booking Cancellation Eligibility: only upcoming confirmed bookings are cancellable")
    void testCancellationEligibility() {
        Booking b = new Booking();

        // 1. Confirmed and Start time is in the future -> Cancellable!
        b.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        b.setStartTime(LocalDateTime.now().plusHours(3));
        b.setEndTime(LocalDateTime.now().plusHours(5));
        assertTrue(b.isEligibleForCancellation());

        // 2. Confirmed but already active or past -> NOT cancellable!
        b.setStartTime(LocalDateTime.now().minusMinutes(10));
        assertFalse(b.isEligibleForCancellation());

        // 3. Already Cancelled -> NOT cancellable!
        b.setBookingStatus(Booking.BookingStatus.CANCELLED);
        b.setStartTime(LocalDateTime.now().plusHours(3));
        assertFalse(b.isEligibleForCancellation());

        // 4. Expired -> NOT cancellable!
        b.setBookingStatus(Booking.BookingStatus.EXPIRED);
        assertFalse(b.isEligibleForCancellation());

        // 5. Completed -> NOT cancellable!
        b.setBookingStatus(Booking.BookingStatus.COMPLETED);
        assertFalse(b.isEligibleForCancellation());
    }

    @Test
    @DisplayName("Booking Active Check: correctly evaluates whether interval is active now")
    void testBookingIsActiveNow() {
        Booking b = new Booking();
        b.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        b.setStartTime(LocalDateTime.now().minusHours(1));
        b.setEndTime(LocalDateTime.now().plusHours(1));

        assertTrue(b.isCurrentlyActive());
        assertFalse(b.isUpcoming());
    }

    @Test
    @DisplayName("ParkingSlot Vehicle Types: handles standard types and fallback gracefully")
    void testSlotVehicleTypes() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleType("EV");
        assertEquals(ParkingSlot.VehicleType.EV, slot.getVehicleType());

        slot.setVehicleType("invalid_type");
        assertEquals(ParkingSlot.VehicleType.CAR, slot.getVehicleType());
    }
}
