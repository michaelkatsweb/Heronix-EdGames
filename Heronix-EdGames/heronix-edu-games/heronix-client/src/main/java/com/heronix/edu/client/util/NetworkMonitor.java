package com.heronix.edu.client.util;

import com.heronix.edu.client.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors network connectivity status
 * Periodically checks if server is reachable
 */
public class NetworkMonitor {
    private static final Logger logger = LoggerFactory.getLogger(NetworkMonitor.class);
    private static final int CHECK_INTERVAL_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    private static NetworkMonitor instance;

    private final AtomicBoolean isOnline = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private final String serverHost;
    private final int serverPort;

    private NetworkMonitor() {
        // Parse server URL to get host and port
        try {
            URI serverUri = new URI(AppConfig.getServerUrl());
            this.serverHost = serverUri.getHost();
            this.serverPort = serverUri.getPort() > 0 ? serverUri.getPort() : 8080;

            logger.info("NetworkMonitor initialized for {}:{}", serverHost, serverPort);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse server URL", e);
        }
    }

    /**
     * Get singleton instance
     */
    public static synchronized NetworkMonitor getInstance() {
        if (instance == null) {
            instance = new NetworkMonitor();
        }
        return instance;
    }

    /**
     * Start monitoring network connectivity
     */
    public void startMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.warn("NetworkMonitor already running");
            return;
        }

        logger.info("Starting network monitoring");

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NetworkMonitor");
            t.setDaemon(true);
            return t;
        });

        // Initial check
        checkConnectivity();

        // Schedule periodic checks
        scheduler.scheduleAtFixedRate(
            this::checkConnectivity,
            CHECK_INTERVAL_SECONDS,
            CHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );

        logger.info("Network monitoring started (checking every {} seconds)", CHECK_INTERVAL_SECONDS);
    }

    /**
     * Stop monitoring
     */
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Network monitoring stopped");
        }
    }

    /**
     * Check if network is currently online
     */
    public boolean isOnline() {
        return isOnline.get();
    }

    /**
     * Perform connectivity check
     */
    private void checkConnectivity() {
        boolean wasOnline = isOnline.get();
        boolean nowOnline = isServerReachable();

        isOnline.set(nowOnline);

        // Log status changes
        if (nowOnline != wasOnline) {
            if (nowOnline) {
                logger.info("Network connectivity restored");
            } else {
                logger.warn("Network connectivity lost");
            }
        }
    }

    /**
     * Check if server is reachable
     */
    private boolean isServerReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(
                new InetSocketAddress(serverHost, serverPort),
                CONNECTION_TIMEOUT_MS
            );
            logger.debug("Server is reachable");
            return true;
        } catch (Exception e) {
            logger.debug("Server not reachable: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Force an immediate connectivity check
     */
    public void checkNow() {
        logger.debug("Manual connectivity check requested");
        checkConnectivity();
    }
}
