package com.smartparking.controller;

import com.smartparking.model.ParkingArea;
import com.smartparking.model.ParkingSlot;
import com.smartparking.service.ParkingService;
import com.smartparking.util.DateTimeUtil;
import com.smartparking.util.FeeCalculator;
import com.smartparking.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for browsing parking facilities and inspecting area details & real-time slot availability.
 */
@WebServlet(name = "BrowseParkingServlet", urlPatterns = {"/browse", "/parking-details"})
public class BrowseParkingServlet extends HttpServlet {

    private final ParkingService parkingService = new ParkingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/parking-details".equals(path)) {
            handleParkingDetails(request, response);
        } else {
            handleBrowse(request, response);
        }
    }

    private void handleBrowse(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String maxPriceStr = request.getParameter("maxPrice");
        BigDecimal maxPrice = null;

        if (maxPriceStr != null && !maxPriceStr.trim().isEmpty()) {
            try {
                maxPrice = new BigDecimal(maxPriceStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        List<ParkingArea> areas = parkingService.searchParkingAreas(keyword, maxPrice, true);

        request.setAttribute("areas", areas);
        request.setAttribute("keyword", keyword);
        request.setAttribute("maxPrice", maxPriceStr);

        request.getRequestDispatcher("/WEB-INF/views/browse.jsp").forward(request, response);
    }

    private void handleParkingDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String areaIdStr = request.getParameter("id");
        if (areaIdStr == null || areaIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/browse");
            return;
        }

        Long areaId;
        try {
            areaId = Long.parseLong(areaIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/browse");
            return;
        }

        ParkingArea area = parkingService.getAreaById(areaId);
        if (area == null) {
            request.setAttribute("errorMessage", "Parking area not found.");
            response.sendRedirect(request.getContextPath() + "/browse");
            return;
        }

        // Query params for interval availability check
        String startStr = request.getParameter("startTime");
        String endStr = request.getParameter("endTime");
        String vehicleType = request.getParameter("vehicleType");

        LocalDateTime start = DateTimeUtil.parseInput(startStr);
        LocalDateTime end = DateTimeUtil.parseInput(endStr);

        List<ParkingSlot> slots;
        BigDecimal estimatedFee = BigDecimal.ZERO;
        String intervalError = null;

        if (start != null && end != null) {
            intervalError = ValidationUtil.validateBookingInterval(start, end);
            if (intervalError == null) {
                slots = parkingService.getAvailableSlots(areaId, start, end, vehicleType);
                estimatedFee = FeeCalculator.calculateFee(start, end, area.getPricePerHour());
            } else {
                slots = parkingService.getAreaSlots(areaId);
            }
        } else {
            slots = parkingService.getAreaSlots(areaId);
        }

        request.setAttribute("area", area);
        request.setAttribute("slots", slots);
        request.setAttribute("startTime", startStr);
        request.setAttribute("endTime", endStr);
        request.setAttribute("vehicleType", vehicleType);
        request.setAttribute("estimatedFee", estimatedFee);
        request.setAttribute("intervalError", intervalError);

        request.getRequestDispatcher("/WEB-INF/views/parking-details.jsp").forward(request, response);
    }
}
