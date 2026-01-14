package com.heronix.edu.server.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket handshake interceptor for game sessions.
 * Validates session codes and extracts connection parameters during WebSocket upgrade.
 *
 * This uses session-code based authentication instead of JWT for simplicity
 * and to support student connections without requiring full authentication.
 */
@Component
public class GameSessionHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // Extract session code and player info from query parameters
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String query = servletRequest.getServletRequest().getQueryString();

            if (query != null) {
                // Parse query parameters
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        String key = pair[0];
                        String value = java.net.URLDecoder.decode(pair[1], "UTF-8");
                        attributes.put(key, value);
                    }
                }
            }

            // Extract session code from path if provided
            String path = servletRequest.getServletRequest().getRequestURI();
            if (path.contains("/ws/game/")) {
                String sessionCode = path.substring(path.lastIndexOf("/") + 1);
                if (!sessionCode.isEmpty() && !sessionCode.equals("game")) {
                    attributes.put("sessionCode", sessionCode);
                }
            }

            // Get client IP for logging/tracking
            String clientIp = getClientIp(servletRequest);
            attributes.put("clientIp", clientIp);

            logger.debug("WebSocket handshake from {} with attributes: {}", clientIp, attributes);
        }

        // Allow all connections - session validation happens at join time
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("WebSocket handshake failed", exception);
        } else {
            logger.debug("WebSocket handshake completed successfully");
        }
    }

    private String getClientIp(ServletServerHttpRequest request) {
        String ip = request.getServletRequest().getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getServletRequest().getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getServletRequest().getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
