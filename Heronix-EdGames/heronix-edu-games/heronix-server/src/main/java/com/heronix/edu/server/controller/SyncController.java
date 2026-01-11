package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.request.SyncUploadRequest;
import com.heronix.edu.server.dto.response.SyncResponse;
import com.heronix.edu.server.security.JwtAuthenticationFilter;
import com.heronix.edu.server.service.SyncService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for score synchronization.
 */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private SyncService syncService;

    /**
     * Get last sync timestamp for device
     * GET /api/sync/last-sync?deviceId={id}
     */
    @GetMapping("/last-sync")
    public ResponseEntity<Map<String, Object>> getLastSync(
            @RequestParam String deviceId,
            Authentication authentication) {

        logger.debug("Last sync request for device: {}", deviceId);

        // Verify authenticated device matches requested device
        JwtAuthenticationFilter.DevicePrincipal principal =
                (JwtAuthenticationFilter.DevicePrincipal) authentication.getPrincipal();

        if (!principal.getDeviceId().equals(deviceId)) {
            logger.warn("Device ID mismatch: {} vs {}", principal.getDeviceId(), deviceId);
            return ResponseEntity.status(403).build();
        }

        LocalDateTime lastSync = syncService.getLastSyncTimestamp(deviceId);

        return ResponseEntity.ok(Map.of(
                "deviceId", deviceId,
                "lastSyncAt", lastSync != null ? lastSync : "Never",
                "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * Upload game scores
     * POST /api/sync/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<SyncResponse> uploadScores(
            @Valid @RequestBody SyncUploadRequest request,
            Authentication authentication) {

        JwtAuthenticationFilter.DevicePrincipal principal =
                (JwtAuthenticationFilter.DevicePrincipal) authentication.getPrincipal();

        String deviceId = principal.getDeviceId();

        logger.info("Score upload from device: {} ({} scores)", deviceId, request.scores().size());

        SyncResponse response = syncService.uploadScores(deviceId, request.scores());

        return ResponseEntity.ok(response);
    }

    /**
     * Mark sync as complete
     * POST /api/sync/complete
     */
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeSyn(
            Authentication authentication) {

        JwtAuthenticationFilter.DevicePrincipal principal =
                (JwtAuthenticationFilter.DevicePrincipal) authentication.getPrincipal();

        String deviceId = principal.getDeviceId();

        logger.debug("Sync complete for device: {}", deviceId);

        syncService.markSyncComplete(deviceId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "deviceId", deviceId,
                "message", "Sync marked as complete",
                "timestamp", LocalDateTime.now()
        ));
    }
}
