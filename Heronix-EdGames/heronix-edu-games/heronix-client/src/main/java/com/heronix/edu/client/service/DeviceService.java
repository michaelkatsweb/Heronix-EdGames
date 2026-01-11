package com.heronix.edu.client.service;

import com.heronix.edu.client.api.HeronixApiClient;
import com.heronix.edu.client.api.dto.AuthResponse;
import com.heronix.edu.client.api.dto.DeviceRegistrationRequest;
import com.heronix.edu.client.api.dto.DeviceRegistrationResponse;
import com.heronix.edu.client.api.dto.DeviceStatusResponse;
import com.heronix.edu.client.config.AppConfig;
import com.heronix.edu.client.db.entity.LocalDevice;
import com.heronix.edu.client.db.repository.DeviceRepository;
import com.heronix.edu.client.security.DeviceIdentifier;
import com.heronix.edu.client.security.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing device lifecycle
 * Handles registration, approval checking, and authentication
 */
public class DeviceService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final HeronixApiClient apiClient;
    private final TokenManager tokenManager;

    public DeviceService(DeviceRepository deviceRepository, TokenManager tokenManager) {
        this.deviceRepository = deviceRepository;
        this.tokenManager = tokenManager;
        this.apiClient = new HeronixApiClient(tokenManager);
    }

    /**
     * Get the device ID, creating one if it doesn't exist
     */
    public String getOrCreateDeviceId() {
        Optional<LocalDevice> device = deviceRepository.findDevice();
        if (device.isPresent()) {
            return device.get().getDeviceId();
        }

        // Generate new device ID
        String deviceId = DeviceIdentifier.generateDeviceId();
        logger.info("Generated new device ID: {}", deviceId);
        return deviceId;
    }

    /**
     * Check if device is registered
     */
    public boolean isDeviceRegistered() {
        return deviceRepository.isDeviceRegistered();
    }

    /**
     * Check if device is approved
     */
    public boolean isDeviceApproved() {
        Optional<LocalDevice> device = deviceRepository.findDevice();
        return device.map(LocalDevice::isApproved).orElse(false);
    }

    /**
     * Register device with the server
     */
    public void registerDevice(String registrationCode, String deviceName) {
        String deviceId = getOrCreateDeviceId();

        logger.info("Registering device {} with code {}", deviceId, registrationCode);

        // Create registration request
        DeviceRegistrationRequest request = new DeviceRegistrationRequest(
            deviceId,
            null, // studentId assigned by server based on registration code
            registrationCode,
            deviceName,
            "DESKTOP",
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            AppConfig.getAppVersion()
        );

        // Register with server
        DeviceRegistrationResponse response = apiClient.registerDevice(request);

        // Save device locally
        LocalDevice device = new LocalDevice();
        device.setDeviceId(deviceId);
        device.setStudentId(response.getStudentId());
        device.setDeviceName(deviceName);
        device.setDeviceType("DESKTOP");
        device.setOsName(System.getProperty("os.name"));
        device.setOsVersion(System.getProperty("os.version"));
        device.setAppVersion(AppConfig.getAppVersion());
        device.setStatus("PENDING");
        device.setRegistrationCode(registrationCode);
        device.setRegisteredAt(LocalDateTime.now());

        deviceRepository.saveOrUpdate(device);

        logger.info("Device registered successfully: {}", deviceId);
    }

    /**
     * Check approval status with server
     * Offline-resilient: Returns cached status if server is unreachable
     */
    public String checkApprovalStatus() {
        LocalDevice device = deviceRepository.findDevice()
            .orElseThrow(() -> new IllegalStateException("Device not registered"));

        logger.debug("Checking approval status for device: {}", device.getDeviceId());

        try {
            // Try to query server
            DeviceStatusResponse response = apiClient.getDeviceStatus(device.getDeviceId());

            // Update local status
            device.setStatus(response.getStatus());
            if ("APPROVED".equals(response.getStatus())) {
                device.setApprovedAt(LocalDateTime.now());
                device.setStudentId(response.getStudentId());
            }

            deviceRepository.saveOrUpdate(device);

            logger.info("Device status updated from server: {}", response.getStatus());
            return response.getStatus();

        } catch (Exception e) {
            // Server unreachable - use cached local status
            logger.warn("Unable to reach server, using cached approval status: {}", device.getStatus(), e);

            // If device was previously approved, keep it approved (offline resilience)
            if (device.isApproved()) {
                logger.info("Device previously approved - allowing offline access");
                return "APPROVED";
            }

            // Otherwise return current status
            return device.getStatus();
        }
    }

    /**
     * Authenticate device and obtain JWT token
     */
    public void authenticateDevice() {
        LocalDevice device = deviceRepository.findDevice()
            .orElseThrow(() -> new IllegalStateException("Device not registered"));

        if (!device.isApproved()) {
            throw new IllegalStateException("Device not approved");
        }

        logger.info("Authenticating device: {}", device.getDeviceId());

        // Authenticate with server
        AuthResponse response = apiClient.authenticateDevice(
            device.getDeviceId(),
            device.getStudentId()
        );

        // Save token
        tokenManager.saveToken(response.getToken(), response.getExpiresAt());

        // Update device record
        device.setJwtToken(response.getToken());
        device.setTokenExpiresAt(response.getExpiresAt());
        device.setLastSyncAt(LocalDateTime.now());

        deviceRepository.saveOrUpdate(device);

        logger.info("Device authenticated successfully");
    }

    /**
     * Re-authenticate if token is expired
     */
    public void refreshTokenIfNeeded() {
        if (!tokenManager.isTokenValid()) {
            logger.info("Token expired, re-authenticating");
            authenticateDevice();
        }
    }

    /**
     * Get current device
     */
    public Optional<LocalDevice> getDevice() {
        return deviceRepository.findDevice();
    }

    /**
     * Get student ID for this device
     */
    public String getStudentId() {
        return deviceRepository.findDevice()
            .map(LocalDevice::getStudentId)
            .orElseThrow(() -> new IllegalStateException("Device not registered"));
    }

    /**
     * Test server connection
     */
    public boolean testServerConnection() {
        try {
            logger.info("Testing server connection");
            apiClient.ping();
            return true;
        } catch (Exception e) {
            logger.error("Server connection test failed", e);
            return false;
        }
    }
}
