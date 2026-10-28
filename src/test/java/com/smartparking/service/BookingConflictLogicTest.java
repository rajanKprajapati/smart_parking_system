package com.smartparking.service;

import com.smartparking.model.Booking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BookingConflictLogicTest {

    private boolean isIntervalConflicting(LocalDateTime existingStart, LocalDateTime existingEnd,
                                          Booking.BookingStatus existingStatus,
                                          LocalDateTime candidateStart, LocalDateTime candidateEnd) {
        // Cancelled, completed, or expired bookings NEVER block future or new bookings
        if (!Booking.BookingStatus.CONFIRMED.equals(existingStatus)) {
            return false;
        }

        // Standard interval intersection: existing.start < candidate.end AND existing.end > candidate.start
        return existingStart.isBefore(candidateEnd) && existingEnd.isAfter(candidateStart);
    }

    @Test
    @DisplayName("Conflict Logic: Exact interval match conflicts")
    void testExactMatchConflict() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 12, 0);

        assertTrue(isIntervalConflicting(start, end, Booking.BookingStatus.CONFIRMED, start, end));
    }

    @Test
    @DisplayName("Conflict Logic: Overlapping during middle of reservation conflicts")
    void testPartialOverlapConflict() {
        LocalDateTime exStart = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime exEnd = LocalDateTime.of(2026, 11, 10, 14, 0);

        // Candidate arrives at 11:00 and leaves at 15:00
        LocalDateTime candStart = LocalDateTime.of(2026, 11, 10, 11, 0);
        LocalDateTime candEnd = LocalDateTime.of(2026, 11, 10, 15, 0);

        assertTrue(isIntervalConflicting(exStart, exEnd, Booking.BookingStatus.CONFIRMED, candStart, candEnd));
    }

    @Test
    @DisplayName("Conflict Logic: Adjacent bookings with seamless handoff do NOT conflict")
    void testAdjacentIntervalsNoConflict() {
        LocalDateTime exStart = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime exEnd = LocalDateTime.of(2026, 11, 10, 12, 0);

        // Candidate 1: 08:00 to 10:00 (ends right when existing starts)
        assertFalse(isIntervalConflicting(exStart, exEnd, Booking.BookingStatus.CONFIRMED,
                LocalDateTime.of(2026, 11, 10, 8, 0), LocalDateTime.of(2026, 11, 10, 10, 0)));

        // Candidate 2: 12:00 to 14:00 (starts right when existing ends)
        assertFalse(isIntervalConflicting(exStart, exEnd, Booking.BookingStatus.CONFIRMED,
                LocalDateTime.of(2026, 11, 10, 12, 0), LocalDateTime.of(2026, 11, 10, 14, 0)));
    }

    @Test
    @DisplayName("Conflict Logic: Cancelled or expired bookings do NOT block slot")
    void testCancelledOrExpiredSlotReleased() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 12, 0);

        // If status was CANCELLED, new booking at the exact same time is allowed
        assertFalse(isIntervalConflicting(start, end, Booking.BookingStatus.CANCELLED, start, end));

        // If status was EXPIRED, new booking is allowed
        assertFalse(isIntervalConflicting(start, end, Booking.BookingStatus.EXPIRED, start, end));

        // If status was COMPLETED, new booking is allowed
        assertFalse(isIntervalConflicting(start, end, Booking.BookingStatus.COMPLETED, start, end));
    }
}
