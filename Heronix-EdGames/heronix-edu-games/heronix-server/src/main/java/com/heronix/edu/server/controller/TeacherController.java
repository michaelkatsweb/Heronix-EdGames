package com.heronix.edu.server.controller;

import com.heronix.edu.common.model.Device;
import com.heronix.edu.server.dto.request.DeviceApprovalRequest;
import com.heronix.edu.server.dto.request.DeviceActionRequest;
import com.heronix.edu.server.dto.request.GenerateCodeRequest;
import com.heronix.edu.server.dto.request.TeacherLoginRequest;
import com.heronix.edu.server.dto.response.TeacherAuthResponse;
import com.heronix.edu.server.entity.RegistrationCodeEntity;
import com.heronix.edu.server.entity.UserEntity;
import com.heronix.edu.server.security.JwtTokenProvider;
import com.heronix.edu.server.service.TeacherService;
import com.heronix.edu.server.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for teacher operations.
 * Handles authentication, device approval, and class management.
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Teacher login
     * POST /api/teacher/login
     */
    @PostMapping("/login")
    public ResponseEntity<TeacherAuthResponse> login(@Valid @RequestBody TeacherLoginRequest request) {
        logger.info("Teacher login attempt: {}", request.username());

        String token = userService.authenticateUser(request.username(), request.password());

        UserEntity user = userService.getUserByUsername(request.username());

        // Convert Date to LocalDateTime
        java.util.Date expirationDate = jwtTokenProvider.calculateExpirationDate();
        LocalDateTime expirationDateTime = LocalDateTime.ofInstant(
                expirationDate.toInstant(),
                java.time.ZoneId.systemDefault()
        );

        TeacherAuthResponse response = new TeacherAuthResponse(
                token,
                expirationDateTime,
                user.getUserId(),
                user.getUsername(),
                user.getRole().toString()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all pending devices awaiting approval
     * GET /api/teacher/devices/pending
     */
    @GetMapping("/devices/pending")
    public ResponseEntity<List<Device>> getPendingDevices() {
        logger.debug("Fetching pending devices");

        List<Device> devices = teacherService.getPendingDevices();

        return ResponseEntity.ok(devices);
    }

    /**
     * Approve a device
     * POST /api/teacher/devices/approve
     */
    @PostMapping("/devices/approve")
    public ResponseEntity<Device> approveDevice(
            @Valid @RequestBody DeviceActionRequest request,
            Authentication authentication) {

        // Extract teacher ID from JWT (stored as deviceId in token for teachers)
        String teacherIdStr = authentication.getName();
        Long teacherId = Long.parseLong(teacherIdStr);

        logger.info("Approving device: {} by teacher: {}", request.deviceId(), teacherId);

        Device device = teacherService.approveDevice(request.deviceId(), teacherId);

        return ResponseEntity.ok(device);
    }

    /**
     * Reject a device
     * POST /api/teacher/devices/reject
     */
    @PostMapping("/devices/reject")
    public ResponseEntity<Device> rejectDevice(
            @Valid @RequestBody DeviceActionRequest request,
            Authentication authentication) {

        String teacherIdStr = authentication.getName();
        Long teacherId = Long.parseLong(teacherIdStr);

        logger.info("Rejecting device: {} by teacher: {}", request.deviceId(), teacherId);

        String reason = request.reason() != null ? request.reason() : "Rejected by teacher";
        Device device = teacherService.rejectDevice(request.deviceId(), teacherId, reason);

        return ResponseEntity.ok(device);
    }

    /**
     * Revoke device access
     * POST /api/teacher/devices/revoke
     */
    @PostMapping("/devices/revoke")
    public ResponseEntity<Device> revokeDevice(
            @Valid @RequestBody DeviceActionRequest request,
            Authentication authentication) {

        String teacherIdStr = authentication.getName();
        Long teacherId = Long.parseLong(teacherIdStr);

        logger.info("Revoking device: {} by teacher: {}", request.deviceId(), teacherId);

        String reason = request.reason() != null ? request.reason() : "Revoked by teacher";
        Device device = teacherService.revokeDevice(request.deviceId(), teacherId, reason);

        return ResponseEntity.ok(device);
    }

    /**
     * Generate registration code
     * POST /api/teacher/codes/generate
     */
    @PostMapping("/codes/generate")
    public ResponseEntity<Map<String, Object>> generateCode(
            @RequestBody GenerateCodeRequest request,
            Authentication authentication) {

        String teacherIdStr = authentication.getName();
        Long teacherId = Long.parseLong(teacherIdStr);

        logger.info("Generating registration code for teacher: {}", teacherId);

        RegistrationCodeEntity code = teacherService.generateRegistrationCode(
                teacherId,
                request.classId(),
                request.maxUses(),
                request.validDays()
        );

        return ResponseEntity.ok(Map.of(
                "code", code.getCode(),
                "maxUses", code.getMaxUses() != null ? code.getMaxUses() : "unlimited",
                "expiresAt", code.getExpiresAt() != null ? code.getExpiresAt() : "never",
                "active", code.getActive(),
                "createdAt", code.getCreatedAt()
        ));
    }

    /**
     * Get all registration codes for teacher
     * GET /api/teacher/codes
     */
    @GetMapping("/codes")
    public ResponseEntity<List<RegistrationCodeEntity>> getCodes(Authentication authentication) {

        String teacherIdStr = authentication.getName();
        Long teacherId = Long.parseLong(teacherIdStr);

        logger.debug("Fetching registration codes for teacher: {}", teacherId);

        List<RegistrationCodeEntity> codes = teacherService.getTeacherRegistrationCodes(teacherId);

        return ResponseEntity.ok(codes);
    }

    /**
     * Deactivate a registration code
     * DELETE /api/teacher/codes/{code}
     */
    @DeleteMapping("/codes/{code}")
    public ResponseEntity<Map<String, Object>> deactivateCode(
            @PathVariable String code,
            Authentication authentication) {

        String teacherIdStr = authentication.getName();
        Long teacherId = Long.parseLong(teacherIdStr);

        logger.info("Deactivating registration code: {} by teacher: {}", code, teacherId);

        teacherService.deactivateRegistrationCode(code, teacherId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registration code deactivated",
                "code", code
        ));
    }

    /**
     * Get devices for a specific student
     * GET /api/teacher/students/{studentId}/devices
     */
    @GetMapping("/students/{studentId}/devices")
    public ResponseEntity<List<Device>> getStudentDevices(@PathVariable String studentId) {

        logger.debug("Fetching devices for student: {}", studentId);

        List<Device> devices = teacherService.getStudentDevices(studentId);

        return ResponseEntity.ok(devices);
    }
}
