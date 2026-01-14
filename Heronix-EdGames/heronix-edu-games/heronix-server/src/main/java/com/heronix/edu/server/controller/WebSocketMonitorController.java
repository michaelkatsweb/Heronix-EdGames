package com.heronix.edu.server.controller;

import com.heronix.edu.server.websocket.GameSessionChannelInterceptor;
import com.heronix.edu.server.websocket.WebSocketEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for monitoring WebSocket connections and server health.
 * Useful for debugging and ensuring the server can handle 500+ connections.
 */
@RestController
@RequestMapping("/api/monitor")
public class WebSocketMonitorController {

    @Autowired
    private WebSocketEventListener eventListener;

    @Autowired
    private GameSessionChannelInterceptor channelInterceptor;

    private final Instant startTime = Instant.now();

    /**
     * Get WebSocket connection statistics.
     */
    @GetMapping("/websocket")
    public ResponseEntity<Map<String, Object>> getWebSocketStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("currentConnections", eventListener.getCurrentConnections());
        stats.put("peakConnections", eventListener.getPeakConnections());
        stats.put("activeSessions", eventListener.getActiveSessions().size());

        // Add channel interceptor stats
        stats.put("channelActiveConnections", channelInterceptor.getActiveConnectionCount());

        return ResponseEntity.ok(stats);
    }

    /**
     * Get detailed server health information.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getServerHealth() {
        Map<String, Object> health = new HashMap<>();

        // Uptime
        Duration uptime = Duration.between(startTime, Instant.now());
        health.put("uptimeSeconds", uptime.getSeconds());
        health.put("uptimeFormatted", formatDuration(uptime));

        // Memory stats
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsedMB", memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        memory.put("heapMaxMB", memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
        memory.put("heapCommittedMB", memoryBean.getHeapMemoryUsage().getCommitted() / (1024 * 1024));
        health.put("memory", memory);

        // Thread stats
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("totalThreads", threadBean.getThreadCount());
        threads.put("peakThreads", threadBean.getPeakThreadCount());
        threads.put("daemonThreads", threadBean.getDaemonThreadCount());
        health.put("threads", threads);

        // WebSocket stats
        Map<String, Object> websocket = new HashMap<>();
        websocket.put("currentConnections", eventListener.getCurrentConnections());
        websocket.put("peakConnections", eventListener.getPeakConnections());
        health.put("websocket", websocket);

        // Runtime info
        Map<String, Object> runtime = new HashMap<>();
        runtime.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        runtime.put("javaVersion", System.getProperty("java.version"));
        health.put("runtime", runtime);

        health.put("status", "healthy");

        return ResponseEntity.ok(health);
    }

    /**
     * Get active session details (for debugging).
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        Map<String, Object> result = new HashMap<>();

        result.put("totalSessions", eventListener.getActiveSessions().size());
        result.put("sessions", eventListener.getActiveSessions());

        return ResponseEntity.ok(result);
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
