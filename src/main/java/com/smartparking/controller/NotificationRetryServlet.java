package com.smartparking.controller;

import com.smartparking.model.User;
import com.smartparking.service.NotificationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller allowing authorized users to trigger safe retries for eligible failed notifications.
 */
@WebServlet(name = "NotificationRetryServlet", urlPatterns = {"/notifications/retry"})
public class NotificationRetryServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("notificationId");
        String returnUrl = request.getParameter("returnUrl");

        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                Long notificationId = Long.parseLong(idStr.trim());
                notificationService.retryNotification(notificationId, user.getUserId());
            } catch (Exception ignored) {}
        }

        if (returnUrl != null && !returnUrl.trim().isEmpty() && returnUrl.startsWith("/")) {
            response.sendRedirect(request.getContextPath() + returnUrl);
        } else if (user.isProvider()) {
            response.sendRedirect(request.getContextPath() + "/provider/notifications");
        } else {
            response.sendRedirect(request.getContextPath() + "/customer/notifications");
        }
    }
}
