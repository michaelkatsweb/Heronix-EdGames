package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.request.DeviceAuthRequest;
import com.heronix.edu.server.dto.response.AuthResponse;
import com.heronix.edu.server.security.JwtTokenProvider;
import com.heronix.edu.server.service.DeviceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate device and get JWT token
     * POST /api/auth/device
     */
    @PostMapping("/device")
    public ResponseEntity<AuthResponse> authenticateDevice(
            @Valid @RequestBody DeviceAuthRequest request) {

        logger.info("Device authentication request: {}", request.deviceId());

        String token = deviceService.authenticateDevice(request.deviceId(), request.studentId());

        // Convert Date to LocalDateTime
        java.util.Date expirationDate = jwtTokenProvider.calculateExpirationDate();
        LocalDateTime expirationDateTime = LocalDateTime.ofInstant(
                expirationDate.toInstant(),
                java.time.ZoneId.systemDefault()
        );

        AuthResponse response = new AuthResponse(
                token,
                expirationDateTime,
                request.deviceId(),
                request.studentId()
        );

        return ResponseEntity.ok(response);
    }
}
