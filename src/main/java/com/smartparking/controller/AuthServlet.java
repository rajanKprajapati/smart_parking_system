package com.smartparking.controller;

import com.smartparking.model.User;
import com.smartparking.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for user registration, authentication, session establishment, and logout.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            handleLogout(request, response);
        } else if ("/register".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
        } else {
            // Already logged in? Redirect to dashboard
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("loggedUser") != null) {
                User user = (User) session.getAttribute("loggedUser");
                redirectUserByRole(response, request.getContextPath(), user, null);
                return;
            }
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/login".equals(path)) {
            handleLogin(request, response);
        } else if ("/register".equals(path)) {
            handleRegister(request, response);
        } else if ("/logout".equals(path)) {
            handleLogout(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        User user = authService.authenticate(email, password);

        if (user != null) {
            // Establish authenticated session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate(); // Regenerate session ID to prevent fixation
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedUser", user);
            session.setMaxInactiveInterval(1800); // 30 minutes session timeout

            redirectUserByRole(response, request.getContextPath(), user, redirect);
        } else {
            request.setAttribute("errorMessage", "Invalid email address or password.");
            request.setAttribute("email", email);
            request.setAttribute("redirect", redirect);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");

        String error = authService.register(fullName, email, password, phone, role);

        if (error == null) {
            // Success: redirect to login with success flash parameter
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } else {
            request.setAttribute("errorMessage", error);
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.setAttribute("role", role);
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login?logged_out=true");
    }

    private void redirectUserByRole(HttpServletResponse response, String contextPath, User user, String redirect)
            throws IOException {
        if (redirect != null && !redirect.trim().isEmpty() && !redirect.contains("://") && redirect.startsWith("/")) {
            response.sendRedirect(contextPath + redirect);
            return;
        }

        if (user.isProvider()) {
            response.sendRedirect(contextPath + "/provider/dashboard");
        } else {
            response.sendRedirect(contextPath + "/customer/dashboard");
        }
    }
}
