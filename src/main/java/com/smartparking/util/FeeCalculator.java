package com.smartparking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calculates estimated and final parking fees based on reservation duration and hourly rate.
 * <p>
 * Billing Policy:
 * 1. Duration is computed in minutes between start time and end time.
 * 2. Billable hours = minutes / 60.0, rounded to 2 decimal places using RoundingMode.HALF_UP.
 * 3. Minimum billable duration is 0.50 hours (30 minutes).
 * 4. Estimated fee = billable hours * price_per_hour, rounded to 2 decimal places.
 * 5. Calculations are strictly executed on the server side to prevent client tampering.
 */
public final class FeeCalculator {

    private static final BigDecimal MIN_HOURS = new BigDecimal("0.50");
    private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

    private FeeCalculator() {
        // Prevent instantiation
    }

    /**
     * Calculates billable duration in hours between two date-time instants.
     *
     * @param start start time
     * @param end   end time
     * @return billable hours rounded to 2 decimal places
     */
    public static BigDecimal calculateDurationHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return ZERO_AMOUNT;
        }
        long minutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        if (hours.compareTo(MIN_HOURS) < 0) {
            return MIN_HOURS;
        }
        return hours;
    }

    /**
     * Computes the total parking fee.
     *
     * @param start        start time
     * @param end          end time
     * @param pricePerHour price per hour from parking area configuration
     * @return total fee rounded to 2 decimal places
     */
    public static BigDecimal calculateFee(LocalDateTime start, LocalDateTime end, BigDecimal pricePerHour) {
        if (pricePerHour == null || pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_AMOUNT;
        }
        BigDecimal durationHours = calculateDurationHours(start, end);
        if (durationHours.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_AMOUNT;
        }
        return durationHours.multiply(pricePerHour).setScale(2, RoundingMode.HALF_UP);
    }
}
