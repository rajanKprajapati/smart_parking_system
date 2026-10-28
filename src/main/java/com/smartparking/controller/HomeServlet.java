package com.smartparking.controller;

import com.smartparking.model.ParkingArea;
import com.smartparking.service.ParkingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for application landing and home page.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {

    private final ParkingService parkingService = new ParkingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Fetch active featured parking areas for homepage showcase
        List<ParkingArea> featuredAreas = parkingService.searchParkingAreas(null, null, true);
        if (featuredAreas.size() > 6) {
            featuredAreas = featuredAreas.subList(0, 6);
        }
        request.setAttribute("featuredAreas", featuredAreas);

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
