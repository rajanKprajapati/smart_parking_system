package com.smartparking.controller;

import com.smartparking.model.Booking;
import com.smartparking.model.DashboardStats;
import com.smartparking.model.Notification;
import com.smartparking.model.ParkingArea;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.User;
import com.smartparking.service.AuthService;
import com.smartparking.service.BookingService;
import com.smartparking.service.NotificationService;
import com.smartparking.service.ParkingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller managing provider operations: dashboard, area management, slot management,
 * customer bookings oversight, provider profile, and notification history.
 */
@WebServlet(name = "ProviderServlet", urlPatterns = {
        "/provider/dashboard",
        "/provider/areas",
        "/provider/slots",
        "/provider/bookings",
        "/provider/booking-details",
        "/provider/verify-payment",
        "/provider/profile",
        "/provider/notifications"
})
public class ProviderServlet extends HttpServlet {

    private final ParkingService parkingService = new ParkingService();
    private final BookingService bookingService = new BookingService();
    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedUser(request);
        String path = request.getServletPath();

        switch (path) {
            case "/provider/dashboard":
                handleDashboard(request, response, user);
                break;
            case "/provider/areas":
                handleAreasGet(request, response, user);
                break;
            case "/provider/slots":
                handleSlotsGet(request, response, user);
                break;
            case "/provider/bookings":
                handleBookingsGet(request, response, user);
                break;
            case "/provider/booking-details":
                handleBookingDetails(request, response, user);
                break;
            case "/provider/profile":
                handleProfileGet(request, response, user);
                break;
            case "/provider/notifications":
                handleNotifications(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/provider/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedUser(request);
        String path = request.getServletPath();

        switch (path) {
            case "/provider/areas":
                handleAreasPost(request, response, user);
                break;
            case "/provider/slots":
                handleSlotsPost(request, response, user);
                break;
            case "/provider/verify-payment":
                handleVerifyPayment(request, response, user);
                break;
            case "/provider/profile":
                handleProfilePost(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/provider/dashboard");
                break;
        }
    }

    private void handleDashboard(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        DashboardStats stats = bookingService.getProviderDashboardStats(user.getUserId());
        List<Booking> recentBookings = bookingService.getProviderBookings(user.getUserId(), "ALL");
        if (recentBookings.size() > 5) {
            recentBookings = recentBookings.subList(0, 5);
        }

        String query = request.getParameter("query");
        if (query != null && !query.trim().isEmpty()) {
            List<Booking> searchResults = bookingService.searchProviderCustomers(user.getUserId(), query.trim());
            request.setAttribute("searchQuery", query.trim());
            request.setAttribute("searchResults", searchResults);
        }

        request.setAttribute("stats", stats);
        request.setAttribute("recentBookings", recentBookings);
        request.getRequestDispatcher("/WEB-INF/views/provider/dashboard.jsp").forward(request, response);
    }

    private void handleAreasGet(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<ParkingArea> areas = parkingService.getProviderAreas(user.getUserId());
        request.setAttribute("areas", areas);
        request.getRequestDispatcher("/WEB-INF/views/provider/manage-areas.jsp").forward(request, response);
    }

    private void handleAreasPost(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("toggle_status".equals(action)) {
            String areaIdParam = request.getParameter("areaId");
            String statusParam = request.getParameter("status");
            if (areaIdParam != null && statusParam != null) {
                try {
                    Long areaId = Long.parseLong(areaIdParam.trim());
                    ParkingArea.Status status = ParkingArea.Status.valueOf(statusParam.toUpperCase().trim());
                    parkingService.toggleAreaStatus(areaId, user.getUserId(), status);
                } catch (Exception ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/provider/areas?updated=true");
            return;
        }

        String areaIdStr = request.getParameter("areaId");
        String name = request.getParameter("parkingName");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        String priceStr = request.getParameter("pricePerHour");
        String operatingDetails = request.getParameter("operatingDetails");

        BigDecimal price = BigDecimal.ZERO;
        try {
            if (priceStr != null) price = new BigDecimal(priceStr.trim());
        } catch (Exception ignored) {}

        ParkingArea area = new ParkingArea();
        area.setProviderId(user.getUserId());
        area.setParkingName(name);
        area.setLocation(location);
        area.setDescription(description);
        area.setPricePerHour(price);
        area.setOperatingDetails(operatingDetails);

        String error;
        if (areaIdStr != null && !areaIdStr.trim().isEmpty()) {
            try {
                area.setParkingAreaId(Long.parseLong(areaIdStr.trim()));
                error = parkingService.updateParkingArea(area);
            } catch (NumberFormatException e) {
                error = "Invalid facility ID.";
            }
        } else {
            error = parkingService.addParkingArea(area);
        }

        if (error == null) {
            response.sendRedirect(request.getContextPath() + "/provider/areas?saved=true");
        } else {
            request.setAttribute("errorMessage", error);
            handleAreasGet(request, response, user);
        }
    }

    private void handleSlotsGet(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String areaIdStr = request.getParameter("areaId");
        List<ParkingArea> areas = parkingService.getProviderAreas(user.getUserId());
        request.setAttribute("areas", areas);

        ParkingArea selectedArea = null;
        List<ParkingSlot> slots = null;

        if (areaIdStr != null && !areaIdStr.trim().isEmpty()) {
            Long areaId = Long.parseLong(areaIdStr.trim());
            selectedArea = areas.stream().filter(a -> a.getParkingAreaId().equals(areaId)).findFirst().orElse(null);
            if (selectedArea != null) {
                slots = parkingService.getAreaSlots(areaId);
            }
        } else if (!areas.isEmpty()) {
            selectedArea = areas.get(0);
            slots = parkingService.getAreaSlots(selectedArea.getParkingAreaId());
        }

        request.setAttribute("selectedArea", selectedArea);
        request.setAttribute("slots", slots);
        request.getRequestDispatcher("/WEB-INF/views/provider/manage-slots.jsp").forward(request, response);
    }

    private void handleSlotsPost(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String areaIdStr = request.getParameter("areaId");

        if (areaIdStr == null || areaIdStr.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Parking facility must be selected.");
            handleSlotsGet(request, response, user);
            return;
        }

        Long areaId;
        try {
            areaId = Long.parseLong(areaIdStr.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid parking facility selection.");
            handleSlotsGet(request, response, user);
            return;
        }

        if ("toggle_status".equals(action)) {
            String slotIdParam = request.getParameter("slotId");
            String statusParam = request.getParameter("status");
            if (slotIdParam != null && statusParam != null) {
                try {
                    Long slotId = Long.parseLong(slotIdParam.trim());
                    ParkingSlot.Status status = ParkingSlot.Status.valueOf(statusParam.toUpperCase().trim());
                    parkingService.toggleSlotStatus(slotId, user.getUserId(), status);
                } catch (Exception ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/provider/slots?areaId=" + areaIdStr + "&updated=true");
            return;
        }

        String slotIdStr = request.getParameter("slotId");
        String slotNumber = request.getParameter("slotNumber");
        String vehicleType = request.getParameter("vehicleType");

        ParkingSlot slot = new ParkingSlot();
        slot.setParkingAreaId(areaId);
        slot.setSlotNumber(slotNumber);
        slot.setVehicleType(vehicleType);

        String error;
        if (slotIdStr != null && !slotIdStr.trim().isEmpty()) {
            try {
                slot.setSlotId(Long.parseLong(slotIdStr.trim()));
                error = parkingService.updateSlot(slot, user.getUserId());
            } catch (NumberFormatException e) {
                error = "Invalid slot ID.";
            }
        } else {
            error = parkingService.addSlot(slot, user.getUserId());
        }

        if (error == null) {
            response.sendRedirect(request.getContextPath() + "/provider/slots?areaId=" + areaIdStr + "&saved=true");
        } else {
            request.setAttribute("errorMessage", error);
            handleSlotsGet(request, response, user);
        }
    }

    private void handleBookingsGet(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) status = "ALL";

        List<Booking> bookings = bookingService.getProviderBookings(user.getUserId(), status);
        request.setAttribute("bookings", bookings);
        request.setAttribute("activeStatus", status);
        request.getRequestDispatcher("/WEB-INF/views/provider/bookings.jsp").forward(request, response);
    }

    private void handleBookingDetails(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/provider/bookings");
            return;
        }

        Long bookingId = Long.parseLong(idStr.trim());
        Booking booking = bookingService.getBookingDetails(bookingId, user.getUserId(), true);

        if (booking == null) {
            response.sendRedirect(request.getContextPath() + "/provider/bookings");
            return;
        }

        request.setAttribute("booking", booking);
        request.getRequestDispatcher("/WEB-INF/views/provider/booking-details.jsp").forward(request, response);
    }

    private void handleProfileGet(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("user", user);
        request.getRequestDispatcher("/WEB-INF/views/provider/profile.jsp").forward(request, response);
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
                request.setAttribute("errorMessage", "Failed to update profile.");
            }
        }

        handleProfileGet(request, response, user);
    }

    private void handleVerifyPayment(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String bookingIdStr = request.getParameter("bookingId");
        String action = request.getParameter("action"); // "approve" or "reject"
        String reference = request.getParameter("paymentReference");

        if (bookingIdStr != null && !bookingIdStr.trim().isEmpty() && action != null) {
            try {
                Long bookingId = Long.parseLong(bookingIdStr.trim());
                Booking.PaymentStatus newStatus = "approve".equalsIgnoreCase(action)
                        ? Booking.PaymentStatus.PAID
                        : Booking.PaymentStatus.FAILED;

                boolean ok = bookingService.updatePaymentStatus(bookingId, user.getUserId(), newStatus, reference);
                if (ok) {
                    response.sendRedirect(request.getContextPath() + "/provider/booking-details?id=" + bookingId + "&paymentUpdated=true");
                    return;
                }
            } catch (Exception ignored) {}
        }
        response.sendRedirect(request.getContextPath() + "/provider/bookings");
    }

    private void handleNotifications(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<Notification> notifications = notificationService.getUserNotifications(user.getUserId());
        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/WEB-INF/views/provider/notifications.jsp").forward(request, response);
    }

    private User getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (User) session.getAttribute("loggedUser") : null;
    }
}
