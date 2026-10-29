package com.smartparking.util;

import com.smartparking.model.Booking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QR Code and Payment Handling Tests")
class QrCodeAndPaymentTest {

    @Test
    @DisplayName("UPI URI Builder: formats correct RFC-compliant URI with parameters")
    void testBuildUpiUri() {
        String uri = QrCodeUtil.buildUpiUri("merchant@upi", "City Garage", new BigDecimal("15.50"), "BK-1001", "Bay A1");
        assertNotNull(uri);
        assertTrue(uri.startsWith("upi://pay?"));
        assertTrue(uri.contains("pa=merchant%40upi"));
        assertTrue(uri.contains("am=15.50"));
        assertTrue(uri.contains("cu=INR"));
        assertTrue(uri.contains("pn=City+Garage") || uri.contains("pn=City%20Garage"));
    }

    @Test
    @DisplayName("UPI URI Builder: applies fallbacks when optional params are null or empty")
    void testBuildUpiUriFallbacks() {
        String uri = QrCodeUtil.buildUpiUri(null, null, null, null, null);
        assertNotNull(uri);
        assertTrue(uri.contains("pa=smartparking%40upi"));
        assertTrue(uri.contains("cu=INR"));
        assertTrue(uri.contains("am=1.00"));
    }

    @Test
    @DisplayName("QR Code SVG Generator: creates valid SVG output with module paths")
    void testGenerateQrSvg() {
        String sampleText = "upi://pay?pa=smartparking@upi&pn=SmartParking&am=20.00&cu=INR";
        String svg = QrCodeUtil.generateQrSvg(sampleText, 200);

        assertNotNull(svg);
        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains("viewBox="));
        assertTrue(svg.contains("<path fill="));
        assertTrue(svg.endsWith("</svg>"));
    }

    @Test
    @DisplayName("Payment State: new bookings default to PENDING, never automatically marked as PAID")
    void testDefaultPaymentStatusIsPending() {
        Booking booking = new Booking();

        // Must default to PENDING
        assertEquals(Booking.PaymentStatus.PENDING, booking.getPaymentStatus());
        assertTrue(booking.isPaymentPending());
        assertFalse(booking.isPaid());
        assertFalse(booking.isPaymentFailed());

        // Generating a QR code or viewing it must NOT change payment status
        String qr = QrCodeUtil.generateQrSvg("upi://pay?pa=test@upi&am=10.00", 150);
        assertNotNull(qr);
        assertEquals(Booking.PaymentStatus.PENDING, booking.getPaymentStatus(),
                "Generating or displaying a QR code must not mark the booking as PAID");
    }

    @Test
    @DisplayName("Payment Status Transitions: properly distinguishes PENDING, PAID, and FAILED")
    void testPaymentStatusTransitions() {
        Booking booking = new Booking();

        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setPaymentReference("UPI-UTR-998877");
        booking.setPaidAt(LocalDateTime.now());
        assertTrue(booking.isPaid());
        assertFalse(booking.isPaymentPending());
        assertFalse(booking.isPaymentFailed());
        assertEquals("UPI-UTR-998877", booking.getPaymentReference());
        assertNotNull(booking.getPaidAt());

        booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
        assertTrue(booking.isPaymentFailed());
        assertFalse(booking.isPaid());
        assertFalse(booking.isPaymentPending());
    }
}
