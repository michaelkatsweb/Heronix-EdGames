package com.heronix.edu.server.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for device authentication.
 */
public record DeviceAuthRequest(
        @NotBlank(message = "Device ID is required")
        String deviceId,

        @NotBlank(message = "Student ID is required")
        String studentId
) {
}
