package com.smartparking.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for Cross-Site Request Forgery (CSRF) protection.
 * Generates and validates cryptographically secure tokens bound to user sessions.
 */
public final class CsrfTokenUtil {

    public static final String CSRF_SESSION_ATTR = "CSRF_TOKEN";
    public static final String CSRF_PARAM_NAME = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokenUtil() {
        // Prevent instantiation
    }

    /**
     * Obtains or generates a cryptographically secure CSRF token for the session.
     *
     * @param session the user HTTP session
     * @return the active CSRF token string
     */
    public static String getToken(HttpSession session) {
        if (session == null) return "";
        synchronized (session) {
            String token = (String) session.getAttribute(CSRF_SESSION_ATTR);
            if (token == null || token.trim().isEmpty()) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(CSRF_SESSION_ATTR, token);
            }
            return token;
        }
    }

    /**
     * Validates whether the incoming request token matches the session token.
     *
     * @param request the incoming HTTP request
     * @return true if valid, false otherwise
     */
    public static boolean isValidToken(HttpServletRequest request) {
        if (request == null) return false;
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null || sessionToken.trim().isEmpty()) return false;

        String requestToken = request.getParameter(CSRF_PARAM_NAME);
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getHeader("X-CSRF-Token");
        }

        return requestToken != null && sessionToken.equals(requestToken.trim());
    }
}
