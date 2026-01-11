package com.heronix.edu.server.controller;

import com.heronix.edu.common.model.Device;
import com.heronix.edu.server.dto.request.DeviceRegistrationRequest;
import com.heronix.edu.server.dto.response.DeviceRegistrationResponse;
import com.heronix.edu.server.security.JwtAuthenticationFilter;
import com.heronix.edu.server.service.DeviceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for device registration and authentication.
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

    /**
     * Register a new device
     * POST /api/device/register
     */
    @PostMapping("/register")
    public ResponseEntity<DeviceRegistrationResponse> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request) {

        logger.info("Device registration request: {}", request.deviceId());

        Device device = deviceService.registerDevice(request);

        DeviceRegistrationResponse response = new DeviceRegistrationResponse(
                device.getDeviceId(),
                device.getStudentId(),
                device.getStatus(),
                device.getRegisteredAt(),
                "Device registered successfully. Awaiting teacher approval."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get device status
     * GET /api/device/status?deviceId={id}
     */
    @GetMapping("/status")
    public ResponseEntity<Device> getDeviceStatus(
            @RequestParam("deviceId") String deviceId,
            Authentication authentication) {

        logger.debug("Device status request: {}", deviceId);

        // Verify authenticated device matches requested device
        if (authentication != null && authentication.getPrincipal() instanceof JwtAuthenticationFilter.DevicePrincipal principal) {
            if (!principal.getDeviceId().equals(deviceId)) {
                logger.warn("Device ID mismatch: {} vs {}", principal.getDeviceId(), deviceId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Device device = deviceService.getDeviceStatus(deviceId);

        return ResponseEntity.ok(device);
    }
}
