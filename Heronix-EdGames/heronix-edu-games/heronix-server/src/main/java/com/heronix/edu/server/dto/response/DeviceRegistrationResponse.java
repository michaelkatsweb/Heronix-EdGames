package com.heronix.edu.server.dto.response;

import com.heronix.edu.common.model.Device;
import java.time.LocalDateTime;

/**
 * Response DTO for device registration.
 */
public record DeviceRegistrationResponse(
        String deviceId,
        String studentId,
        Device.DeviceStatus status,
        LocalDateTime registeredAt,
        String message
) {
}
