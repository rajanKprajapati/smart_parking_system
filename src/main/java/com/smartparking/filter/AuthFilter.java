package com.smartparking.filter;

import com.smartparking.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter enforcing session-based authentication and role-based access control.
 * Restricts /customer/*, /provider/*, and /admin/* routes to authorized roles.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/customer/*", "/provider/*", "/admin/*", "/notifications/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());

        // 1. Not logged in -> redirect to login
        if (loggedUser == null) {
            String redirectParam = request.getQueryString() != null ? path + "?" + request.getQueryString() : path;
            session = request.getSession(true);
            session.setAttribute("authError", "Please sign in to access that page.");
            response.sendRedirect(contextPath + "/login?redirect=" + java.net.URLEncoder.encode(redirectParam, "UTF-8"));
            return;
        }

        // 2. Account inactive or suspended -> terminate session
        if (!loggedUser.isActive()) {
            session.invalidate();
            response.sendRedirect(contextPath + "/login?error=account_suspended");
            return;
        }

        // 3. Role-based Authorization checks
        if (path.startsWith("/customer/")) {
            if (!loggedUser.isCustomer() && !loggedUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Customer role required.");
                return;
            }
        } else if (path.startsWith("/provider/")) {
            if (!loggedUser.isProvider() && !loggedUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Parking Provider role required.");
                return;
            }
        } else if (path.startsWith("/admin/")) {
            if (!loggedUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator role required.");
                return;
            }
        }

        // Authorized
        chain.doFilter(request, response);
    }
}
