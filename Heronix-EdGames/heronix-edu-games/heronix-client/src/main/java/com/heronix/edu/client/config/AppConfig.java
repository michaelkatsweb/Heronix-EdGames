package com.heronix.edu.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration manager
 * Loads settings from application.properties
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Application configuration loaded");
            } else {
                logger.warn("application.properties not found, using defaults");
            }
        } catch (IOException e) {
            logger.error("Failed to load application.properties", e);
        }
    }

    /**
     * Get server URL
     */
    public static String getServerUrl() {
        return properties.getProperty("heronix.server.url", "http://localhost:8080");
    }

    /**
     * Get database path
     */
    public static String getDbPath() {
        String path = properties.getProperty("heronix.db.path",
            System.getProperty("user.home") + "/.heronix/client.db");
        return path.replace("${user.home}", System.getProperty("user.home"));
    }

    /**
     * Get sync interval in minutes
     */
    public static int getSyncIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("heronix.sync.interval.minutes", "5"));
    }

    /**
     * Get sync batch size
     */
    public static int getSyncBatchSize() {
        return Integer.parseInt(properties.getProperty("heronix.sync.batch.size", "100"));
    }

    /**
     * Get games directory
     */
    public static String getGamesDirectory() {
        String path = properties.getProperty("heronix.games.directory",
            System.getProperty("user.home") + "/.heronix/games");
        return path.replace("${user.home}", System.getProperty("user.home"));
    }

    /**
     * Get database path (alias for getDbPath)
     */
    public static String getDatabasePath() {
        return getDbPath();
    }

    /**
     * Get application version
     */
    public static String getAppVersion() {
        return properties.getProperty("heronix.app.version", "1.0.0");
    }
}
