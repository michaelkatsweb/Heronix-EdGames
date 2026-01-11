package com.heronix.edu.client.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages the local H2 database for offline storage
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_DIRECTORY = System.getProperty("user.home") + File.separator + ".heronix";
    private static final String DB_PATH = DB_DIRECTORY + File.separator + "client.db";
    private static HikariDataSource dataSource;
    private static boolean initialized = false;

    /**
     * Initialize the database connection pool and schema
     */
    public static synchronized void initialize() {
        if (initialized) {
            logger.debug("Database already initialized");
            return;
        }

        try {
            // Create directory if it doesn't exist
            File dbDir = new File(DB_DIRECTORY);
            if (!dbDir.exists()) {
                boolean created = dbDir.mkdirs();
                if (created) {
                    logger.info("Created database directory: {}", DB_DIRECTORY);
                }
            }

            // Configure H2 connection pool
            HikariConfig config = new HikariConfig();
            // Removed AUTO_SERVER and DB_CLOSE_ON_EXIT as they conflict in H2 2.x
            config.setJdbcUrl("jdbc:h2:file:" + DB_PATH);
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);

            logger.info("Database connection pool initialized at: {}", DB_PATH);

            // Mark as initialized before executing schema (needed for getConnection)
            initialized = true;

            // Execute schema
            executeSchema();

            logger.info("Database initialization completed successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Execute the schema.sql file to create tables
     */
    private static void executeSchema() {
        logger.debug("Executing database schema");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String schema = loadResourceFile("/schema.sql");
            if (schema == null || schema.isEmpty()) {
                throw new RuntimeException("Schema file is empty or not found");
            }

            // Execute schema (H2 can handle multiple statements)
            stmt.execute(schema);

            logger.info("Database schema executed successfully");

        } catch (SQLException e) {
            logger.error("Failed to execute schema", e);
            throw new RuntimeException("Schema execution failed", e);
        }
    }

    /**
     * Load a resource file from classpath as a string
     */
    private static String loadResourceFile(String resourcePath) {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            logger.error("Failed to load resource file: {}", resourcePath, e);
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    /**
     * Get a database connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new IllegalStateException("Database not initialized. Call initialize() first.");
        }
        return dataSource.getConnection();
    }

    /**
     * Shutdown the database connection pool
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Shutting down database connection pool");
            dataSource.close();
            initialized = false;
            logger.info("Database shutdown completed");
        }
    }

    /**
     * Check if database is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get the database file path
     */
    public static String getDatabasePath() {
        return DB_PATH;
    }
}
