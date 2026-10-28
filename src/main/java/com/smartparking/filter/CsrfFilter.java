package com.smartparking.filter;

import com.smartparking.util.CsrfTokenUtil;
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
 * Filter providing Cross-Site Request Forgery (CSRF) protection.
 * Ensures CSRF tokens are injected for view rendering and verifies tokens on state-changing requests.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = {"/*"})
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Ensure session exists and attach CSRF token to request attributes
        HttpSession session = request.getSession(true);
        String token = CsrfTokenUtil.getToken(session);
        request.setAttribute("csrfToken", token);

        String method = request.getMethod();

        // Validate CSRF on state-changing requests
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String path = uri.substring(contextPath.length());

            // Protected state-changing paths
            boolean needsCsrf = path.startsWith("/customer/") ||
                                path.startsWith("/provider/") ||
                                path.startsWith("/notifications/") ||
                                "/login".equals(path) ||
                                "/register".equals(path);

            if (needsCsrf && !CsrfTokenUtil.isValidToken(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF validation failed. Invalid or missing security token.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
