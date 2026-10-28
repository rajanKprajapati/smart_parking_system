package com.smartparking.controller;

import com.smartparking.model.Booking;
import com.smartparking.model.Notification;
import com.smartparking.model.ParkingArea;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.User;
import com.smartparking.service.AuthService;
import com.smartparking.service.BookingService;
import com.smartparking.service.NotificationService;
import com.smartparking.service.ParkingService;
import com.smartparking.util.DateTimeUtil;
import com.smartparking.util.FeeCalculator;
import com.smartparking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller managing customer operations: dashboard, booking, cancellations, profile, and notification history.
 */
@WebServlet(name = "CustomerServlet", urlPatterns = {
        "/customer/dashboard",
        "/customer/book",
        "/customer/confirmation",
        "/customer/bookings",
        "/customer/cancel-booking",
        "/customer/profile",
        "/customer/notifications"
})
public class CustomerServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();
    private final ParkingService parkingService = new ParkingService();
    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedUser(request);
        String path = request.getServletPath();

        switch (path) {
            case "/customer/dashboard":
                handleDashboard(request, response, user);
                break;
            case "/customer/book":
                handleBookingForm(request, response, user);
                break;
            case "/customer/confirmation":
                handleConfirmation(request, response, user);
                break;
            case "/customer/bookings":
                handleBookingsList(request, response, user);
                break;
            case "/customer/profile":
                handleProfileGet(request, response, user);
                break;
            case "/customer/notifications":
                handleNotifications(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedUser(request);
        String path = request.getServletPath();

        switch (path) {
            case "/customer/book":
                handleBookingSubmit(request, response, user);
                break;
            case "/customer/cancel-booking":
                handleBookingCancel(request, response, user);
                break;
            case "/customer/profile":
                handleProfilePost(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
                break;
        }
    }

    private void handleDashboard(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<Booking> recentBookings = bookingService.getCustomerBookings(user.getUserId(), "ALL");
        List<ParkingArea> availableAreas = parkingService.searchParkingAreas(null, null, true);

        request.setAttribute("recentBookings", recentBookings);
        request.setAttribute("availableAreas", availableAreas);
        request.getRequestDispatcher("/WEB-INF/views/customer/dashboard.jsp").forward(request, response);
    }

    private void handleBookingForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String areaIdStr = request.getParameter("areaId");
        String slotIdStr = request.getParameter("slotId");
        String startStr = request.getParameter("startTime");
        String endStr = request.getParameter("endTime");

        if (areaIdStr == null || areaIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/browse");
            return;
        }

        Long areaId = Long.parseLong(areaIdStr.trim());
        ParkingArea area = parkingService.getAreaById(areaId);
        if (area == null || !area.isActive()) {
            response.sendRedirect(request.getContextPath() + "/browse");
            return;
        }

        LocalDateTime start = DateTimeUtil.parseInput(startStr);
        LocalDateTime end = DateTimeUtil.parseInput(endStr);
        List<ParkingSlot> availableSlots = null;
        BigDecimal estimatedFee = BigDecimal.ZERO;

        if (start != null && end != null) {
            String intervalError = ValidationUtil.validateBookingInterval(start, end);
            if (intervalError == null) {
                availableSlots = parkingService.getAvailableSlots(areaId, start, end, null);
                estimatedFee = FeeCalculator.calculateFee(start, end, area.getPricePerHour());
            } else {
                request.setAttribute("errorMessage", intervalError);
            }
        } else {
            availableSlots = parkingService.getAreaSlots(areaId);
        }

        request.setAttribute("area", area);
        request.setAttribute("slotId", slotIdStr);
        request.setAttribute("availableSlots", availableSlots);
        request.setAttribute("startTime", startStr);
        request.setAttribute("endTime", endStr);
        request.setAttribute("estimatedFee", estimatedFee);

        request.getRequestDispatcher("/WEB-INF/views/customer/booking-form.jsp").forward(request, response);
    }

    private void handleBookingSubmit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String areaIdStr = request.getParameter("areaId");
        String slotIdStr = request.getParameter("slotId");
        String startStr = request.getParameter("startTime");
        String endStr = request.getParameter("endTime");

        try {
            Long areaId = Long.parseLong(areaIdStr.trim());
            Long slotId = Long.parseLong(slotIdStr.trim());
            LocalDateTime start = DateTimeUtil.parseInput(startStr);
            LocalDateTime end = DateTimeUtil.parseInput(endStr);

            Booking booking = bookingService.createBooking(user.getUserId(), areaId, slotId, start, end);
            response.sendRedirect(request.getContextPath() + "/customer/confirmation?ref=" + booking.getBookingReference());
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            handleBookingForm(request, response, user);
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "Database error occurred while saving your booking. Please try again.");
            handleBookingForm(request, response, user);
        }
    }

    private void handleConfirmation(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String ref = request.getParameter("ref");
        if (ref == null || ref.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        List<Booking> list = bookingService.getCustomerBookings(user.getUserId(), "ALL");
        Booking target = list.stream().filter(b -> ref.equalsIgnoreCase(b.getBookingReference())).findFirst().orElse(null);

        if (target == null) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        request.setAttribute("booking", target);
        request.getRequestDispatcher("/WEB-INF/views/customer/booking-confirmation.jsp").forward(request, response);
    }

    private void handleBookingsList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "ALL";
        }

        List<Booking> bookings = bookingService.getCustomerBookings(user.getUserId(), status);
        request.setAttribute("bookings", bookings);
        request.setAttribute("activeStatus", status);

        request.getRequestDispatcher("/WEB-INF/views/customer/bookings.jsp").forward(request, response);
    }

    private void handleBookingCancel(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String bookingIdStr = request.getParameter("bookingId");
        String reason = request.getParameter("reason");

        try {
            Long bookingId = Long.parseLong(bookingIdStr.trim());
            boolean ok = bookingService.cancelBooking(bookingId, user.getUserId(), reason);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/customer/bookings?cancelled=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/customer/bookings?error=cannot_cancel");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleProfileGet(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("user", user);
        request.getRequestDispatcher("/WEB-INF/views/customer/profile.jsp").forward(request, response);
    }

    private void handleProfilePost(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("change_password".equals(action)) {
            String currentPass = request.getParameter("currentPassword");
            String newPass = request.getParameter("newPassword");
            String err = authService.changePassword(user.getUserId(), currentPass, newPass);
            if (err == null) {
                request.setAttribute("successMessage", "Password updated successfully.");
            } else {
                request.setAttribute("errorMessage", err);
            }
        } else {
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phoneNumber");
            boolean ok = authService.updateProfile(user.getUserId(), fullName, phone);
            if (ok) {
                user.setFullName(fullName.trim());
                user.setPhoneNumber(phone.trim());
                request.setAttribute("successMessage", "Profile updated successfully.");
            } else {
                request.setAttribute("errorMessage", "Failed to update profile. Please verify all inputs.");
            }
        }

        handleProfileGet(request, response, user);
    }

    private void handleNotifications(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<Notification> notifications = notificationService.getUserNotifications(user.getUserId());
        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/WEB-INF/views/customer/notifications.jsp").forward(request, response);
    }

    private User getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (User) session.getAttribute("loggedUser") : null;
    }
}
