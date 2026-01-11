package com.heronix.edu.client.api.dto;

/**
 * Request DTO for device registration
 */
public class DeviceRegistrationRequest {
    private String deviceId;
    private String studentId; // Assigned by server based on registration code
    private String registrationCode;
    private String deviceName;
    private String deviceType;
    private String osName;
    private String osVersion;
    private String appVersion;

    public DeviceRegistrationRequest() {
    }

    public DeviceRegistrationRequest(String deviceId, String studentId, String registrationCode,
                                   String deviceName, String deviceType, String osName,
                                   String osVersion, String appVersion) {
        this.deviceId = deviceId;
        this.studentId = studentId;
        this.registrationCode = registrationCode;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.osName = osName;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
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

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
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
}
