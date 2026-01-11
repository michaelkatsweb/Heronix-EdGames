package com.heronix.edu.server.service;

import com.heronix.edu.common.model.Device;
import com.heronix.edu.server.dto.request.DeviceRegistrationRequest;
import com.heronix.edu.server.entity.DeviceEntity;
import com.heronix.edu.server.entity.RegistrationCodeEntity;
import com.heronix.edu.server.exception.DeviceNotApprovedException;
import com.heronix.edu.server.exception.InvalidRegistrationCodeException;
import com.heronix.edu.server.exception.ResourceNotFoundException;
import com.heronix.edu.server.repository.DeviceRepository;
import com.heronix.edu.server.repository.RegistrationCodeRepository;
import com.heronix.edu.server.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Service for device registration and management.
 */
@Service
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RegistrationCodeRepository registrationCodeRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SisApiClient sisApiClient;

    /**
     * Register a new device
     *
     * NEW WORKFLOW (Hardware-Based):
     * 1. Device ID is now a hardware fingerprint (CPU/MAC/Motherboard)
     * 2. Registration code is OPTIONAL (can be null)
     * 3. Student only enters their name (deviceName)
     * 4. Teacher approves and assigns student ID
     * 5. Hardware ID becomes the authentication credential
     */
    @Transactional
    public Device registerDevice(DeviceRegistrationRequest request) {
        logger.info("Registering device: {} (name: {})", request.deviceId(), request.deviceName());

        // Validate registration code ONLY if provided (backward compatibility)
        RegistrationCodeEntity code = null;
        if (request.registrationCode() != null && !request.registrationCode().trim().isEmpty()) {
            code = registrationCodeRepository.findByCode(request.registrationCode())
                    .orElseThrow(() -> new InvalidRegistrationCodeException(request.registrationCode(), "Code not found"));

            if (!code.isValid()) {
                String reason = !code.getActive() ? "inactive" :
                        (code.getExpiresAt() != null && LocalDateTime.now().isAfter(code.getExpiresAt())) ? "expired" :
                                "max uses reached";
                throw new InvalidRegistrationCodeException(request.registrationCode(), reason);
            }
            logger.info("Registration code validated: {}", request.registrationCode());
        } else {
            logger.info("No registration code provided - using hardware-based registration");
        }

        // Check if device already registered
        if (deviceRepository.findByDeviceId(request.deviceId()).isPresent()) {
            throw new IllegalArgumentException("Device already registered: " + request.deviceId());
        }

        // Create device entity
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId(request.deviceId());  // Hardware-based fingerprint
        device.setStudentId(request.studentId());
        device.setDeviceName(request.deviceName());  // Student's entered name
        device.setDeviceType(request.deviceType());
        device.setOperatingSystem(request.operatingSystem());
        device.setOsVersion(request.osVersion());
        device.setAppVersion(request.appVersion());
        device.setRegistrationCode(request.registrationCode());  // May be null
        device.setStatus(Device.DeviceStatus.PENDING);
        device.setRegisteredAt(LocalDateTime.now());
        device.setActive(true);

        DeviceEntity saved = deviceRepository.save(device);

        // Increment registration code usage ONLY if code was provided
        if (code != null) {
            code.incrementUses();
            registrationCodeRepository.save(code);
        }

        auditService.logDeviceRegistration(request.deviceId(), request.studentId(), "SUCCESS");

        logger.info("Device registered successfully: {} (name: {}, status: PENDING)",
                    request.deviceId(), request.deviceName());

        return saved.toModel();
    }

    /**
     * Authenticate device and generate JWT token
     */
    @Transactional
    public String authenticateDevice(String deviceId, String studentId) {
        logger.info("Authenticating device: {}", deviceId);

        DeviceEntity device = deviceRepository.findByDeviceIdAndStudentId(deviceId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        // Check if device is approved
        if (device.getStatus() != Device.DeviceStatus.APPROVED || !device.getActive()) {
            auditService.logDeviceAuthentication(deviceId, "FAILURE");
            throw new DeviceNotApprovedException(deviceId, device.getStatus().toString());
        }

        // Generate or refresh token
        String token = jwtTokenProvider.generateToken(deviceId, studentId);
        Date expirationDate = jwtTokenProvider.calculateExpirationDate();

        // Update device with new token
        device.setAuthToken(token);
        device.setTokenExpiresAt(convertToLocalDateTime(expirationDate));
        deviceRepository.save(device);

        auditService.logDeviceAuthentication(deviceId, "SUCCESS");

        logger.info("Device authenticated successfully: {}", deviceId);

        return token;
    }

    /**
     * Get device status
     */
    @Transactional(readOnly = true)
    public Device getDeviceStatus(String deviceId) {
        logger.debug("Fetching device status: {}", deviceId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        return device.toModel();
    }

    /**
     * Check if device is approved
     */
    @Transactional(readOnly = true)
    public boolean isDeviceApproved(String deviceId) {
        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElse(null);

        return device != null &&
                device.getStatus() == Device.DeviceStatus.APPROVED &&
                device.getActive();
    }

    /**
     * Update last sync timestamp
     */
    @Transactional
    public void updateLastSync(String deviceId, LocalDateTime timestamp) {
        logger.debug("Updating last sync for device: {}", deviceId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        device.setLastSyncAt(timestamp);
        deviceRepository.save(device);
    }

    /**
     * Get device by ID
     */
    @Transactional(readOnly = true)
    public Device getDevice(String deviceId) {
        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        return device.toModel();
    }

    /**
     * Convert Date to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    // ==================== Device Management Methods (for Teacher Portal) ====================

    /**
     * Get all pending devices awaiting approval
     */
    @Transactional(readOnly = true)
    public java.util.List<Device> getPendingDevices() {
        logger.debug("Fetching all pending devices");
        return deviceRepository.findByStatus(Device.DeviceStatus.PENDING)
                .stream()
                .map(DeviceEntity::toModel)
                .toList();
    }

    /**
     * Get all active/approved devices
     */
    @Transactional(readOnly = true)
    public java.util.List<Device> getActiveDevices() {
        logger.debug("Fetching all active devices");
        return deviceRepository.findByStatusAndActiveTrue(Device.DeviceStatus.APPROVED)
                .stream()
                .map(DeviceEntity::toModel)
                .toList();
    }

    /**
     * Get all devices for a specific student
     */
    @Transactional(readOnly = true)
    public java.util.List<Device> getDevicesByStudentId(String studentId) {
        logger.debug("Fetching devices for student: {}", studentId);
        return deviceRepository.findByStudentId(studentId)
                .stream()
                .map(DeviceEntity::toModel)
                .toList();
    }

    /**
     * Get device by ID (alias for getDevice)
     */
    @Transactional(readOnly = true)
    public Device getDeviceById(String deviceId) {
        return getDevice(deviceId);
    }

    /**
     * Approve a device and assign it to a student
     */
    @Transactional
    public Device approveDevice(String deviceId, String studentId) {
        logger.info("Approving device: {} for student: {}", deviceId, studentId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        if (device.getStatus() != Device.DeviceStatus.PENDING) {
            throw new IllegalStateException("Only PENDING devices can be approved. Current status: " + device.getStatus());
        }

        // Validate student ID against SIS (unless it's the test student "testgm1")
        if (!"testgm1".equalsIgnoreCase(studentId)) {
            boolean isValidStudent = sisApiClient.validateStudentId(studentId);
            if (!isValidStudent) {
                logger.warn("Invalid student ID: {} - not found in SIS", studentId);
                throw new IllegalArgumentException("Student ID not found in Student Information System: " + studentId);
            }
            logger.info("Student ID validated successfully: {}", studentId);
        } else {
            logger.info("Using test student ID: {}", studentId);
        }

        // Assign student and approve
        device.setStudentId(studentId);
        device.setStatus(Device.DeviceStatus.APPROVED);
        device.setApprovedAt(LocalDateTime.now());
        device.setActive(true);

        DeviceEntity saved = deviceRepository.save(device);

        auditService.logDeviceApproval(deviceId, studentId, "APPROVED");

        logger.info("Device approved successfully: {} -> student: {}", deviceId, studentId);

        return saved.toModel();
    }

    /**
     * Reject a device registration
     */
    @Transactional
    public Device rejectDevice(String deviceId, String reason) {
        logger.info("Rejecting device: {} - reason: {}", deviceId, reason);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        device.setStatus(Device.DeviceStatus.REJECTED);
        device.setActive(false);
        device.setRejectedAt(LocalDateTime.now());

        DeviceEntity saved = deviceRepository.save(device);

        auditService.logDeviceApproval(deviceId, device.getStudentId(), "REJECTED: " + (reason != null ? reason : "No reason provided"));

        logger.info("Device rejected: {}", deviceId);

        return saved.toModel();
    }

    /**
     * Revoke an approved device
     */
    @Transactional
    public Device revokeDevice(String deviceId, String reason) {
        logger.info("Revoking device: {} - reason: {}", deviceId, reason);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        if (device.getStatus() != Device.DeviceStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED devices can be revoked. Current status: " + device.getStatus());
        }

        device.setStatus(Device.DeviceStatus.REVOKED);
        device.setActive(false);
        device.setAuthToken(null); // Invalidate token

        DeviceEntity saved = deviceRepository.save(device);

        auditService.logDeviceRevocation(deviceId, device.getStudentId(), reason != null ? reason : "No reason provided");

        logger.info("Device revoked: {}", deviceId);

        return saved.toModel();
    }

    /**
     * Delete a device completely
     */
    @Transactional
    public void deleteDevice(String deviceId) {
        logger.warn("Deleting device: {}", deviceId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        deviceRepository.delete(device);

        auditService.logDeviceDeletion(deviceId, device.getStudentId());

        logger.info("Device deleted: {}", deviceId);
    }

    /**
     * Count pending devices
     */
    @Transactional(readOnly = true)
    public long countPendingDevices() {
        return deviceRepository.countByStatus(Device.DeviceStatus.PENDING);
    }

    /**
     * Count approved devices
     */
    @Transactional(readOnly = true)
    public long countApprovedDevices() {
        return deviceRepository.countByStatusAndActiveTrue(Device.DeviceStatus.APPROVED);
    }

    /**
     * Count rejected devices
     */
    @Transactional(readOnly = true)
    public long countRejectedDevices() {
        return deviceRepository.countByStatus(Device.DeviceStatus.REJECTED);
    }

    /**
     * Count revoked devices
     */
    @Transactional(readOnly = true)
    public long countRevokedDevices() {
        return deviceRepository.countByStatus(Device.DeviceStatus.REVOKED);
    }

    /**
     * Count all devices
     */
    @Transactional(readOnly = true)
    public long countAllDevices() {
        return deviceRepository.count();
    }
}
