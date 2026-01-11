package com.heronix.edu.server.service;

import com.heronix.edu.server.entity.AuditLogEntity;
import com.heronix.edu.server.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for FERPA-compliant audit logging.
 * Records all data access and modifications for compliance purposes.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Value("${heronix.audit.enabled:true}")
    private boolean auditEnabled;

    /**
     * Log a data access or modification event
     */
    @Transactional
    public void logEvent(String userId, String action, String entityType, String entityId, String result) {
        if (!auditEnabled) {
            return;
        }

        try {
            AuditLogEntity auditLog = new AuditLogEntity(userId, action, entityType, entityId, result);
            auditLogRepository.save(auditLog);
            logger.debug("Audit log created: {} - {} on {} {}", userId, action, entityType, entityId);
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
            // Don't throw exception - audit logging should not break application flow
        }
    }

    /**
     * Log a device registration event
     */
    public void logDeviceRegistration(String deviceId, String studentId, String result) {
        logEvent(deviceId, "DEVICE_REGISTRATION", "Device", deviceId, result);
    }

    /**
     * Log a device authentication event
     */
    public void logDeviceAuthentication(String deviceId, String result) {
        logEvent(deviceId, "DEVICE_AUTHENTICATION", "Device", deviceId, result);
    }

    /**
     * Log a score sync event
     */
    public void logScoreSync(String deviceId, int scoreCount, String result) {
        String details = String.format("Synced %d scores", scoreCount);
        AuditLogEntity auditLog = new AuditLogEntity(deviceId, "SCORE_SYNC", "GameScore", null, result);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    /**
     * Log a data access event for student data
     */
    public void logStudentDataAccess(String userId, String studentId, String action) {
        logEvent(userId, action, "Student", studentId, "SUCCESS");
    }

    /**
     * Log device approval event
     */
    public void logDeviceApproval(String deviceId, String studentId, String result) {
        String details = String.format("Device %s assigned to student %s", deviceId, studentId);
        AuditLogEntity auditLog = new AuditLogEntity(deviceId, "DEVICE_APPROVAL", "Device", deviceId, result);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    /**
     * Log device revocation event
     */
    public void logDeviceRevocation(String deviceId, String studentId, String reason) {
        String details = String.format("Device %s revoked for student %s - Reason: %s", deviceId, studentId, reason);
        AuditLogEntity auditLog = new AuditLogEntity(deviceId, "DEVICE_REVOCATION", "Device", deviceId, "REVOKED");
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    /**
     * Log device deletion event
     */
    public void logDeviceDeletion(String deviceId, String studentId) {
        String details = String.format("Device %s deleted (was assigned to student %s)", deviceId, studentId);
        AuditLogEntity auditLog = new AuditLogEntity("SYSTEM", "DEVICE_DELETION", "Device", deviceId, "DELETED");
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }
}
