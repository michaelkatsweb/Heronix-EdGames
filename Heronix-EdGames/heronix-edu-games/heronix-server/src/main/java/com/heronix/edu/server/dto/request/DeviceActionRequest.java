package com.heronix.edu.server.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for device actions (reject, revoke) that include a reason
 */
public record DeviceActionRequest(
        @NotBlank(message = "Device ID is required")
        String deviceId,

        String reason  // Optional reason for rejection/revocation
) {
}
