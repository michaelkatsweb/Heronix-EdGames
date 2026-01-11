package com.heronix.edu.client.api.dto;

/**
 * Request DTO for device authentication
 */
public class DeviceAuthRequest {
    private String deviceId;
    private String studentId;

    public DeviceAuthRequest() {
    }

    public DeviceAuthRequest(String deviceId, String studentId) {
        this.deviceId = deviceId;
        this.studentId = studentId;
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
}
