package com.heronix.edu.client.db.repository;

import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.entity.LocalDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for device data
 * Handles CRUD operations for the device table (single row)
 */
public class DeviceRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeviceRepository.class);

    /**
     * Find the device record (there should only be one)
     */
    public Optional<LocalDevice> findDevice() {
        String sql = "SELECT * FROM device LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapResultSetToDevice(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding device", e);
            throw new RuntimeException("Failed to find device", e);
        }
    }

    /**
     * Save or update the device record
     */
    public void saveOrUpdate(LocalDevice device) {
        String sql = "MERGE INTO device (device_id, device_name, device_type, os_name, os_version, " +
                     "app_version, status, registration_code, student_id, registered_at, approved_at, " +
                     "last_sync_at, jwt_token, token_expires_at) " +
                     "KEY(device_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, device.getDeviceId());
            pstmt.setString(2, device.getDeviceName());
            pstmt.setString(3, device.getDeviceType());
            pstmt.setString(4, device.getOsName());
            pstmt.setString(5, device.getOsVersion());
            pstmt.setString(6, device.getAppVersion());
            pstmt.setString(7, device.getStatus());
            pstmt.setString(8, device.getRegistrationCode());
            pstmt.setString(9, device.getStudentId());
            pstmt.setTimestamp(10, toTimestamp(device.getRegisteredAt()));
            pstmt.setTimestamp(11, toTimestamp(device.getApprovedAt()));
            pstmt.setTimestamp(12, toTimestamp(device.getLastSyncAt()));
            pstmt.setString(13, device.getJwtToken());
            pstmt.setTimestamp(14, toTimestamp(device.getTokenExpiresAt()));

            pstmt.executeUpdate();
            logger.debug("Device saved/updated: {}", device.getDeviceId());

        } catch (SQLException e) {
            logger.error("Error saving device", e);
            throw new RuntimeException("Failed to save device", e);
        }
    }

    /**
     * Check if device is registered
     */
    public boolean isDeviceRegistered() {
        return findDevice().isPresent();
    }

    /**
     * Delete the device record
     */
    public void delete() {
        String sql = "DELETE FROM device";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            logger.info("Device record deleted");

        } catch (SQLException e) {
            logger.error("Error deleting device", e);
            throw new RuntimeException("Failed to delete device", e);
        }
    }

    /**
     * Map ResultSet to LocalDevice entity
     */
    private LocalDevice mapResultSetToDevice(ResultSet rs) throws SQLException {
        LocalDevice device = new LocalDevice();
        device.setDeviceId(rs.getString("device_id"));
        device.setDeviceName(rs.getString("device_name"));
        device.setDeviceType(rs.getString("device_type"));
        device.setOsName(rs.getString("os_name"));
        device.setOsVersion(rs.getString("os_version"));
        device.setAppVersion(rs.getString("app_version"));
        device.setStatus(rs.getString("status"));
        device.setRegistrationCode(rs.getString("registration_code"));
        device.setStudentId(rs.getString("student_id"));
        device.setRegisteredAt(toLocalDateTime(rs.getTimestamp("registered_at")));
        device.setApprovedAt(toLocalDateTime(rs.getTimestamp("approved_at")));
        device.setLastSyncAt(toLocalDateTime(rs.getTimestamp("last_sync_at")));
        device.setJwtToken(rs.getString("jwt_token"));
        device.setTokenExpiresAt(toLocalDateTime(rs.getTimestamp("token_expires_at")));
        return device;
    }

    /**
     * Convert LocalDateTime to Timestamp
     */
    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    /**
     * Convert Timestamp to LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
