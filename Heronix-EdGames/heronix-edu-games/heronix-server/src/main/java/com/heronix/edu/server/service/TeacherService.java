package com.heronix.edu.server.service;

import com.heronix.edu.common.model.Device;
import com.heronix.edu.server.entity.DeviceEntity;
import com.heronix.edu.server.entity.RegistrationCodeEntity;
import com.heronix.edu.server.exception.ResourceNotFoundException;
import com.heronix.edu.server.repository.DeviceRepository;
import com.heronix.edu.server.repository.RegistrationCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for teacher-specific operations.
 * Handles device approval, registration code generation, etc.
 */
@Service
public class TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RegistrationCodeRepository registrationCodeRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Get all pending devices for approval
     */
    @Transactional(readOnly = true)
    public List<Device> getPendingDevices() {
        logger.debug("Fetching pending devices");

        return deviceRepository.findByStatus(Device.DeviceStatus.PENDING)
                .stream()
                .map(DeviceEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Approve a device
     */
    @Transactional
    public Device approveDevice(String deviceId, Long teacherId) {
        logger.info("Approving device: {} by teacher: {}", deviceId, teacherId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        if (device.getStatus() != Device.DeviceStatus.PENDING) {
            throw new IllegalStateException("Device is not in PENDING status");
        }

        device.setStatus(Device.DeviceStatus.APPROVED);
        device.setApprovedAt(LocalDateTime.now());
        device.setApprovedBy(teacherId.toString());

        DeviceEntity saved = deviceRepository.save(device);

        auditService.logEvent(teacherId.toString(), "DEVICE_APPROVED", "Device", deviceId, "SUCCESS");

        logger.info("Device approved: {}", deviceId);

        return saved.toModel();
    }

    /**
     * Reject a device
     */
    @Transactional
    public Device rejectDevice(String deviceId, Long teacherId, String reason) {
        logger.info("Rejecting device: {} by teacher: {}", deviceId, teacherId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        device.setStatus(Device.DeviceStatus.REJECTED);
        device.setActive(false);
        device.setDeactivationReason(reason);

        DeviceEntity saved = deviceRepository.save(device);

        auditService.logEvent(teacherId.toString(), "DEVICE_REJECTED", "Device", deviceId, "SUCCESS");

        logger.info("Device rejected: {}", deviceId);

        return saved.toModel();
    }

    /**
     * Revoke device access
     */
    @Transactional
    public Device revokeDevice(String deviceId, Long teacherId, String reason) {
        logger.info("Revoking device: {} by teacher: {}", deviceId, teacherId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        device.setStatus(Device.DeviceStatus.REVOKED);
        device.setActive(false);
        device.setDeactivationReason(reason);

        DeviceEntity saved = deviceRepository.save(device);

        auditService.logEvent(teacherId.toString(), "DEVICE_REVOKED", "Device", deviceId, "SUCCESS");

        logger.info("Device revoked: {}", deviceId);

        return saved.toModel();
    }

    /**
     * Generate a registration code for a class
     */
    @Transactional
    public RegistrationCodeEntity generateRegistrationCode(Long teacherId, Long classId,
                                                           Integer maxUses, Integer validDays) {
        logger.info("Generating registration code for teacher: {}, class: {}", teacherId, classId);

        // Generate a random 8-character code
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LocalDateTime expiresAt = validDays != null ?
                LocalDateTime.now().plusDays(validDays) : null;

        RegistrationCodeEntity regCode = new RegistrationCodeEntity();
        regCode.setCode(code);
        regCode.setTeacherId(teacherId.toString());
        regCode.setClassId(classId != null ? classId.toString() : null);
        regCode.setMaxUses(maxUses);
        regCode.setTimesUsed(0);
        regCode.setExpiresAt(expiresAt);
        regCode.setActive(true);

        RegistrationCodeEntity saved = registrationCodeRepository.save(regCode);

        auditService.logEvent(teacherId.toString(), "REGISTRATION_CODE_CREATED",
                "RegistrationCode", code, "SUCCESS");

        logger.info("Registration code generated: {}", code);

        return saved;
    }

    /**
     * Get all registration codes for a teacher
     */
    @Transactional(readOnly = true)
    public List<RegistrationCodeEntity> getTeacherRegistrationCodes(Long teacherId) {
        logger.debug("Fetching registration codes for teacher: {}", teacherId);

        return registrationCodeRepository.findByTeacherId(teacherId.toString());
    }

    /**
     * Deactivate a registration code
     */
    @Transactional
    public void deactivateRegistrationCode(String code, Long teacherId) {
        logger.info("Deactivating registration code: {} by teacher: {}", code, teacherId);

        RegistrationCodeEntity regCode = registrationCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("RegistrationCode", code));

        regCode.setActive(false);
        registrationCodeRepository.save(regCode);

        auditService.logEvent(teacherId.toString(), "REGISTRATION_CODE_DEACTIVATED",
                "RegistrationCode", code, "SUCCESS");
    }

    /**
     * Get devices by student ID (for teacher to view student's devices)
     */
    @Transactional(readOnly = true)
    public List<Device> getStudentDevices(String studentId) {
        logger.debug("Fetching devices for student: {}", studentId);

        return deviceRepository.findByStudentId(studentId)
                .stream()
                .map(DeviceEntity::toModel)
                .collect(Collectors.toList());
    }
}
