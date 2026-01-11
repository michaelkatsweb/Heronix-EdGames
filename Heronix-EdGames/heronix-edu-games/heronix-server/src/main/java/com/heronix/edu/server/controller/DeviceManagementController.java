package com.heronix.edu.server.controller;

import com.heronix.edu.common.model.Device;
import com.heronix.edu.server.dto.request.DeviceApprovalRequest;
import com.heronix.edu.server.dto.response.ApiResponse;
import com.heronix.edu.server.service.DeviceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for device management operations (for Teacher Portal)
 * Provides endpoints for teachers to approve/reject/manage student devices
 */
@RestController
@RequestMapping("/api/device/management")
public class DeviceManagementController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManagementController.class);
    private final DeviceService deviceService;

    public DeviceManagementController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Get all pending device registrations awaiting approval
     * Requires TEACHER or ADMIN role
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Device>> getPendingDevices(Authentication authentication) {
        String requester = authentication != null ? (authentication != null ? authentication.getName() : "anonymous") : "anonymous";
        logger.info("Fetching pending devices - requested by: {}", requester);
        List<Device> pendingDevices = deviceService.getPendingDevices();
        return ResponseEntity.ok(pendingDevices);
    }

    /**
     * Get all active/approved devices
     * Requires TEACHER or ADMIN role
     */
    @GetMapping("/active")
    public ResponseEntity<List<Device>> getActiveDevices(Authentication authentication) {
        String requester = authentication != null ? (authentication != null ? authentication.getName() : "anonymous") : "anonymous";
        logger.info("Fetching active devices - requested by: {}", requester);
        List<Device> activeDevices = deviceService.getActiveDevices();
        return ResponseEntity.ok(activeDevices);
    }

    /**
     * Get all devices (any status) for a specific student
     * Requires TEACHER or ADMIN role
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Device>> getDevicesByStudent(
            @PathVariable String studentId,
            Authentication authentication) {
        logger.info("Fetching devices for student {} - requested by: {}", studentId, (authentication != null ? authentication.getName() : "anonymous"));
        List<Device> devices = deviceService.getDevicesByStudentId(studentId);
        return ResponseEntity.ok(devices);
    }

    /**
     * Get device details by device ID
     * Requires TEACHER or ADMIN role
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDevice(
            @PathVariable String deviceId,
            Authentication authentication) {
        logger.info("Fetching device {} - requested by: {}", deviceId, (authentication != null ? authentication.getName() : "anonymous"));
        Device device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(device);
    }

    /**
     * Approve a device and assign it to a student
     * Requires TEACHER or ADMIN role
     */
    @PostMapping("/{deviceId}/approve")
    public ResponseEntity<ApiResponse<Device>> approveDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceApprovalRequest request,
            Authentication authentication) {
        logger.info("Approving device {} for student {} - approved by: {}",
                deviceId, request.studentId(), (authentication != null ? authentication.getName() : "anonymous"));

        Device approvedDevice = deviceService.approveDevice(deviceId, request.studentId());

        return ResponseEntity.ok(ApiResponse.success(
                "Device approved successfully",
                approvedDevice
        ));
    }

    /**
     * Reject a device registration
     * Requires TEACHER or ADMIN role
     */
    @PostMapping("/{deviceId}/reject")
    public ResponseEntity<ApiResponse<Device>> rejectDevice(
            @PathVariable String deviceId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        logger.info("Rejecting device {} - rejected by: {} - reason: {}",
                deviceId, (authentication != null ? authentication.getName() : "anonymous"), reason);

        Device rejectedDevice = deviceService.rejectDevice(deviceId, reason);

        return ResponseEntity.ok(ApiResponse.success(
                "Device rejected",
                rejectedDevice
        ));
    }

    /**
     * Revoke/deactivate an approved device
     * Requires TEACHER or ADMIN role
     */
    @PostMapping("/{deviceId}/revoke")
    public ResponseEntity<ApiResponse<Device>> revokeDevice(
            @PathVariable String deviceId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        logger.info("Revoking device {} - revoked by: {} - reason: {}",
                deviceId, (authentication != null ? authentication.getName() : "anonymous"), reason);

        Device revokedDevice = deviceService.revokeDevice(deviceId, reason);

        return ResponseEntity.ok(ApiResponse.success(
                "Device access revoked",
                revokedDevice
        ));
    }

    /**
     * Delete a device registration completely
     * Requires ADMIN role only
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @PathVariable String deviceId,
            Authentication authentication) {
        logger.warn("Deleting device {} - deleted by: {}", deviceId, (authentication != null ? authentication.getName() : "anonymous"));

        deviceService.deleteDevice(deviceId);

        return ResponseEntity.ok(ApiResponse.success(
                "Device deleted successfully",
                null
        ));
    }

    /**
     * Get device statistics (counts by status)
     * Requires TEACHER or ADMIN role
     */
    @GetMapping("/stats")
    public ResponseEntity<DeviceStats> getDeviceStats(Authentication authentication) {
        logger.info("Fetching device statistics - requested by: {}", (authentication != null ? authentication.getName() : "anonymous"));

        long pendingCount = deviceService.countPendingDevices();
        long approvedCount = deviceService.countApprovedDevices();
        long rejectedCount = deviceService.countRejectedDevices();
        long revokedCount = deviceService.countRevokedDevices();
        long totalCount = deviceService.countAllDevices();

        DeviceStats stats = new DeviceStats(
                totalCount,
                pendingCount,
                approvedCount,
                rejectedCount,
                revokedCount
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Inner record for device statistics
     */
    public record DeviceStats(
            long total,
            long pending,
            long approved,
            long rejected,
            long revoked
    ) {}
}
