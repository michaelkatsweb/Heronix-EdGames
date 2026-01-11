package com.heronix.edu.server.exception;

/**
 * Exception thrown when a device is not approved for synchronization.
 */
public class DeviceNotApprovedException extends RuntimeException {

    public DeviceNotApprovedException(String message) {
        super(message);
    }

    public DeviceNotApprovedException(String deviceId, String status) {
        super(String.format("Device %s is not approved (status: %s)", deviceId, status));
    }
}
