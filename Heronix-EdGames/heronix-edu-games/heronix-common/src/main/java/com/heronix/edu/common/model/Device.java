package com.heronix.edu.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a registered device in the system.
 * Only approved devices can sync with the server.
 */
public class Device {
    
    /**
     * Unique device identifier (UUID generated on device)
     */
    private String deviceId;
    
    /**
     * Student associated with this device
     */
    private String studentId;
    
    /**
     * Device name/description (e.g., "John's Laptop", "Family iPad")
     */
    private String deviceName;
    
    /**
     * Device type (DESKTOP, MOBILE, TABLET)
     */
    private DeviceType deviceType;
    
    /**
     * Operating system (Windows, macOS, Linux, Android, iOS)
     */
    private String operatingSystem;
    
    /**
     * OS version
     */
    private String osVersion;
    
    /**
     * Application version installed
     */
    private String appVersion;
    
    /**
     * Registration status
     */
    private DeviceStatus status;
    
    /**
     * Registration code used (provided by teacher)
     */
    private String registrationCode;
    
    /**
     * When device was registered
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registeredAt;
    
    /**
     * When device was approved by teacher
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;
    
    /**
     * Teacher/admin who approved
     */
    private String approvedBy;
    
    /**
     * Last time device synced with server
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastSyncAt;
    
    /**
     * Whether device is currently active
     */
    private boolean active;
    
    /**
     * Reason for deactivation (if applicable)
     */
    private String deactivationReason;
    
    /**
     * Authentication token for this device
     */
    private String authToken;
    
    /**
     * Token expiration time
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tokenExpiresAt;
    
    public enum DeviceType {
        DESKTOP, LAPTOP, MOBILE, TABLET, UNKNOWN
    }
    
    public enum DeviceStatus {
        PENDING,    // Registered but not yet approved
        APPROVED,   // Approved and active
        REJECTED,   // Registration rejected
        REVOKED     // Previously approved but now revoked
    }
    
    // Constructors
    public Device() {
        this.deviceId = UUID.randomUUID().toString();
        this.status = DeviceStatus.PENDING;
        this.registeredAt = LocalDateTime.now();
        this.active = true;
    }
    
    public Device(String studentId, String deviceName, DeviceType deviceType) {
        this();
        this.studentId = studentId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
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
    
    public DeviceType getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(DeviceType deviceType) {
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
    
    public DeviceStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeviceStatus status) {
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
    
    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }
    
    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
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
    
    /**
     * Check if device is approved and active
     */
    public boolean isAuthorized() {
        return active && status == DeviceStatus.APPROVED;
    }
    
    /**
     * Check if auth token is still valid
     */
    public boolean isTokenValid() {
        if (authToken == null || tokenExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(tokenExpiresAt);
    }
    
    /**
     * Approve this device
     */
    public void approve(String approvedBy) {
        this.status = DeviceStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedBy;
    }
    
    /**
     * Revoke device access
     */
    public void revoke(String reason) {
        this.status = DeviceStatus.REVOKED;
        this.active = false;
        this.deactivationReason = reason;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(deviceId, device.deviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }
    
    @Override
    public String toString() {
        return "Device{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType=" + deviceType +
                ", status=" + status +
                ", active=" + active +
                '}';
    }
}
