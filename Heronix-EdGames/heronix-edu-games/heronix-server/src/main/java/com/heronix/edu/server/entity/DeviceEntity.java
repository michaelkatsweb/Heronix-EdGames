package com.heronix.edu.server.entity;

import com.heronix.edu.common.model.Device;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity for Device data.
 * Maps to the 'devices' table in the database.
 * Tracks device registration, approval status, and authentication tokens.
 */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id", unique = true),
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class DeviceEntity {

    @Id
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Column(name = "student_id", nullable = true, length = 50)
    private String studentId;

    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private Device.DeviceType deviceType;

    @Column(name = "operating_system", length = 50)
    private String operatingSystem;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Device.DeviceStatus status = Device.DeviceStatus.PENDING;

    @Column(name = "registration_code", length = 50)
    private String registrationCode;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "deactivation_reason", length = 500)
    private String deactivationReason;

    @Column(name = "auth_token", length = 500)
    private String authToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    // Constructors
    public DeviceEntity() {
    }

    public DeviceEntity(String deviceId, String studentId, String deviceName, Device.DeviceType deviceType) {
        this.deviceId = deviceId;
        this.studentId = studentId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.status = Device.DeviceStatus.PENDING;
        this.registeredAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * Convert Device domain model to DeviceEntity
     */
    public static DeviceEntity fromModel(Device device) {
        DeviceEntity entity = new DeviceEntity();
        entity.setDeviceId(device.getDeviceId());
        entity.setStudentId(device.getStudentId());
        entity.setDeviceName(device.getDeviceName());
        entity.setDeviceType(device.getDeviceType());
        entity.setOperatingSystem(device.getOperatingSystem());
        entity.setOsVersion(device.getOsVersion());
        entity.setAppVersion(device.getAppVersion());
        entity.setStatus(device.getStatus());
        entity.setRegistrationCode(device.getRegistrationCode());
        entity.setRegisteredAt(device.getRegisteredAt());
        entity.setApprovedAt(device.getApprovedAt());
        entity.setApprovedBy(device.getApprovedBy());
        entity.setLastSyncAt(device.getLastSyncAt());
        entity.setActive(device.isActive());
        entity.setDeactivationReason(device.getDeactivationReason());
        entity.setAuthToken(device.getAuthToken());
        entity.setTokenExpiresAt(device.getTokenExpiresAt());
        return entity;
    }

    /**
     * Convert DeviceEntity to Device domain model
     */
    public Device toModel() {
        Device device = new Device();
        device.setDeviceId(this.deviceId);
        device.setStudentId(this.studentId);
        device.setDeviceName(this.deviceName);
        device.setDeviceType(this.deviceType);
        device.setOperatingSystem(this.operatingSystem);
        device.setOsVersion(this.osVersion);
        device.setAppVersion(this.appVersion);
        device.setStatus(this.status);
        device.setRegistrationCode(this.registrationCode);
        device.setRegisteredAt(this.registeredAt);
        device.setApprovedAt(this.approvedAt);
        device.setApprovedBy(this.approvedBy);
        device.setLastSyncAt(this.lastSyncAt);
        device.setActive(this.active);
        device.setDeactivationReason(this.deactivationReason);
        device.setAuthToken(this.authToken);
        device.setTokenExpiresAt(this.tokenExpiresAt);
        return device;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Device.DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Device.DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Device.DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(Device.DeviceStatus status) {
        this.status = status;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public void setDeactivationReason(String deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceEntity that = (DeviceEntity) o;
        return Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }

    @Override
    public String toString() {
        return "DeviceEntity{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType=" + deviceType +
                ", status=" + status +
                ", active=" + active +
                '}';
    }
}
