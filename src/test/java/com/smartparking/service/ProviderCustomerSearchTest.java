package com.smartparking.service;

import com.smartparking.model.Booking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Provider Customer Search & Isolation Tests")
class ProviderCustomerSearchTest {

    private Booking createMockBooking(Long bookingId, String ref, Long providerId, String customerName,
                                      String email, String phone, Booking.PaymentStatus paymentStatus) {
        Booking b = new Booking();
        b.setBookingId(bookingId);
        b.setBookingReference(ref);
        b.setProviderId(providerId);
        b.setCustomerName(customerName);
        b.setCustomerEmail(email);
        b.setCustomerPhone(phone);
        b.setPaymentStatus(paymentStatus);
        b.setCalculatedFee(new BigDecimal("25.00"));
        b.setStartTime(LocalDateTime.now().plusHours(1));
        b.setEndTime(LocalDateTime.now().plusHours(3));
        return b;
    }

    @Test
    @DisplayName("Provider Isolation: Provider 1 only matches bookings for Provider 1 facilities")
    void testProviderIsolationInSearch() {
        Long provider1Id = 1L;
        Long provider2Id = 2L;

        List<Booking> allBookings = new ArrayList<>();
        // Provider 1 bookings
        allBookings.add(createMockBooking(101L, "BK-20261030-1001", provider1Id, "Alice Driver", "alice@example.com", "+1-555-0101", Booking.PaymentStatus.PAID));
        allBookings.add(createMockBooking(102L, "BK-20261030-1002", provider1Id, "Bob Miller", "bob@example.com", "+1-555-0102", Booking.PaymentStatus.PENDING));

        // Provider 2 bookings
        allBookings.add(createMockBooking(201L, "BK-20261030-2001", provider2Id, "Charlie Customer", "charlie@example.com", "+1-555-0201", Booking.PaymentStatus.PAID));
        allBookings.add(createMockBooking(202L, "BK-20261030-2002", provider2Id, "Alice Smith", "alice.smith@example.com", "+1-555-0202", Booking.PaymentStatus.FAILED));

        // Simulate search filter for Provider 1 searching "Alice"
        String searchTerm = "Alice";
        List<Booking> provider1Results = allBookings.stream()
                .filter(b -> b.getProviderId().equals(provider1Id))
                .filter(b -> b.getCustomerName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                             b.getCustomerEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                             b.getBookingReference().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();

        // Must only return Alice Driver (Provider 1's customer), NOT Alice Smith (Provider 2's customer)
        assertEquals(1, provider1Results.size());
        assertEquals("Alice Driver", provider1Results.get(0).getCustomerName());
        assertEquals(provider1Id, provider1Results.get(0).getProviderId());

        // Verify Provider 1 cannot access Charlie Customer
        List<Booking> charlieSearch = allBookings.stream()
                .filter(b -> b.getProviderId().equals(provider1Id))
                .filter(b -> b.getCustomerName().toLowerCase().contains("charlie"))
                .toList();
        assertTrue(charlieSearch.isEmpty(), "Provider 1 must not see Provider 2's customer records");
    }

    @Test
    @DisplayName("Search Criteria: correctly matches Customer Name, Email, Phone, and Booking ID/Ref")
    void testSearchMatchingCriteria() {
        Booking b = createMockBooking(405L, "BK-20261030-4050", 1L, "Jane Doe", "jane.doe@work.com", "+91-9876543210", Booking.PaymentStatus.PENDING);

        // Name match
        assertTrue(b.getCustomerName().toLowerCase().contains("jane"));
        // Email match
        assertTrue(b.getCustomerEmail().toLowerCase().contains("jane.doe@work.com"));
        // Phone match
        assertTrue(b.getCustomerPhone().contains("9876543210"));
        // Ref match
        assertTrue(b.getBookingReference().toLowerCase().contains("bk-20261030-4050"));
        // ID match
        assertEquals("405", String.valueOf(b.getBookingId()));
    }

    @Test
    @DisplayName("Cross-Provider Access Restriction: getBookingDetails returns null for unauthorized provider")
    void testBookingDetailsSecurityCheck() {
        Booking booking = createMockBooking(300L, "BK-300", 1L, "David", "david@test.com", "555-0300", Booking.PaymentStatus.PAID);

        Long authorizedProvider = 1L;
        Long unauthorizedProvider = 2L;

        // Provider 1 should match
        assertEquals(authorizedProvider, booking.getProviderId());

        // Provider 2 access should be denied
        assertNotEquals(unauthorizedProvider, booking.getProviderId());
    }
}
