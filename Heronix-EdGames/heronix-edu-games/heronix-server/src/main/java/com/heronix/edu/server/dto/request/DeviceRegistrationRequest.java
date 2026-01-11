package com.heronix.edu.server.dto.request;

import com.heronix.edu.common.model.Device;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for device registration.
 *
 * IMPORTANT: Device ID is now hardware-based (CPU ID, MAC address, motherboard serial)
 * This eliminates the need for registration codes - the device itself is the credential.
 *
 * Students only need to enter their name (deviceName) and the system automatically
 * captures the unique hardware ID for device fingerprinting.
 */
public record DeviceRegistrationRequest(
        @NotBlank(message = "Device ID is required")
        String deviceId,  // Hardware-based device fingerprint (SHA-256 hash)

        // Student ID is optional - teacher assigns during approval
        String studentId,

        // Registration code is now OPTIONAL (kept for backward compatibility)
        // In the new workflow, this can be null - hardware ID is the credential
        String registrationCode,

        @NotBlank(message = "Device name is required")
        String deviceName,  // Student enters their name (first/last/nickname)

        @NotNull(message = "Device type is required")
        Device.DeviceType deviceType,

        String operatingSystem,
        String osVersion,
        String appVersion
) {
}
