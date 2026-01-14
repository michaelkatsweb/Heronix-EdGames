package com.heronix.edu.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT Authentication Filter.
 * Intercepts requests and validates JWT tokens in the Authorization header.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String deviceId = tokenProvider.getDeviceIdFromToken(jwt);
                String studentId = tokenProvider.getStudentIdFromToken(jwt);

                // Create authentication object with device credentials
                DevicePrincipal principal = new DevicePrincipal(deviceId, studentId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, jwt, new ArrayList<>());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Set authentication for device: {} (student: {})", deviceId, studentId);
            }
        } catch (Exception ex) {
            logger.error("Could not set device authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7).trim();

            // Basic JWT format validation - must have exactly 2 periods (header.payload.signature)
            if (token.isEmpty() || countPeriods(token) != 2) {
                logger.debug("Rejecting malformed token (invalid format) from: {}", request.getRemoteAddr());
                return null;
            }

            return token;
        }
        return null;
    }

    /**
     * Count period characters in a string (for JWT format validation)
     */
    private int countPeriods(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '.') count++;
        }
        return count;
    }

    /**
     * Inner class representing device principal for authentication
     */
    public static class DevicePrincipal {
        private final String deviceId;
        private final String studentId;

        public DevicePrincipal(String deviceId, String studentId) {
            this.deviceId = deviceId;
            this.studentId = studentId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getStudentId() {
            return studentId;
        }

        @Override
        public String toString() {
            return "Device{" + deviceId + ", Student:" + studentId + "}";
        }
    }
}
