package com.heronix.edu.client.security;

/**
 * Utility for generating stable device identifiers
 * Delegates to HardwareIdentifier for hardware-based device fingerprinting
 */
public class DeviceIdentifier {

    /**
     * Generate a unique, persistent device ID based on hardware characteristics
     * This creates a stable identifier that persists across application restarts
     * and uniquely identifies the physical device.
     *
     * @return Hardware-based device identifier (SHA-256 hash)
     */
    public static String generateDeviceId() {
        return HardwareIdentifier.generateDeviceId();
    }

    /**
     * Get a human-readable summary of device hardware
     *
     * @return Device hardware information summary
     */
    public static String getDeviceHardwareSummary() {
        return HardwareIdentifier.getDeviceHardwareSummary();
    }
}
