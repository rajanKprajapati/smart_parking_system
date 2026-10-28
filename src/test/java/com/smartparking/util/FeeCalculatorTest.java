package com.smartparking.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FeeCalculatorTest {

    @Test
    @DisplayName("Fee calculation: 2 exact hours at $5.00/hr = $10.00")
    void testExactTwoHours() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 1, 12, 0);
        BigDecimal pricePerHour = new BigDecimal("5.00");

        BigDecimal hours = FeeCalculator.calculateDurationHours(start, end);
        BigDecimal fee = FeeCalculator.calculateFee(start, end, pricePerHour);

        assertEquals(new BigDecimal("2.00"), hours);
        assertEquals(new BigDecimal("10.00"), fee);
    }

    @Test
    @DisplayName("Fee calculation: 1 hour 30 mins at $6.00/hr = $9.00")
    void testFractionalHours() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 1, 15, 30);
        BigDecimal pricePerHour = new BigDecimal("6.00");

        BigDecimal hours = FeeCalculator.calculateDurationHours(start, end);
        BigDecimal fee = FeeCalculator.calculateFee(start, end, pricePerHour);

        assertEquals(new BigDecimal("1.50"), hours);
        assertEquals(new BigDecimal("9.00"), fee);
    }

    @Test
    @DisplayName("Fee calculation: 15 mins satisfies minimum billing duration policy (0.50 hrs)")
    void testMinimumBillingPolicy() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 1, 8, 15);
        BigDecimal pricePerHour = new BigDecimal("10.00");

        BigDecimal hours = FeeCalculator.calculateDurationHours(start, end);
        BigDecimal fee = FeeCalculator.calculateFee(start, end, pricePerHour);

        assertEquals(new BigDecimal("0.50"), hours);
        assertEquals(new BigDecimal("5.00"), fee);
    }

    @Test
    @DisplayName("Fee calculation: invalid or inverted interval returns zero")
    void testInvalidInterval() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 1, 9, 0);
        BigDecimal pricePerHour = new BigDecimal("5.00");

        assertEquals(new BigDecimal("0.00"), FeeCalculator.calculateDurationHours(start, end));
        assertEquals(new BigDecimal("0.00"), FeeCalculator.calculateFee(start, end, pricePerHour));
    }
}
