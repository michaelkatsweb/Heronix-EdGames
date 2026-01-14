package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.response.ApiResponse;
import com.heronix.edu.server.entity.GameBundleEntity;
import com.heronix.edu.server.entity.SchoolLicenseEntity;
import com.heronix.edu.server.service.BundleLicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for game bundles and school licenses.
 * Provides endpoints for viewing bundles, checking access, and managing licenses.
 */
@RestController
@RequestMapping("/api/bundles")
public class BundleController {

    private static final Logger logger = LoggerFactory.getLogger(BundleController.class);

    private final BundleLicenseService bundleLicenseService;

    public BundleController(BundleLicenseService bundleLicenseService) {
        this.bundleLicenseService = bundleLicenseService;
    }

    /**
     * Get all available bundles
     * GET /api/bundles
     */
    @GetMapping
    public ResponseEntity<List<GameBundleEntity>> getAllBundles() {
        logger.debug("Fetching all bundles");
        List<GameBundleEntity> bundles = bundleLicenseService.getAllBundles();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get standard (free) bundles
     * GET /api/bundles/standard
     */
    @GetMapping("/standard")
    public ResponseEntity<List<GameBundleEntity>> getStandardBundles() {
        logger.debug("Fetching standard bundles");
        List<GameBundleEntity> bundles = bundleLicenseService.getStandardBundles();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get premium bundles
     * GET /api/bundles/premium
     */
    @GetMapping("/premium")
    public ResponseEntity<List<GameBundleEntity>> getPremiumBundles() {
        logger.debug("Fetching premium bundles");
        List<GameBundleEntity> bundles = bundleLicenseService.getPremiumBundles();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get a specific bundle
     * GET /api/bundles/{bundleId}
     */
    @GetMapping("/{bundleId}")
    public ResponseEntity<GameBundleEntity> getBundle(@PathVariable String bundleId) {
        logger.debug("Fetching bundle: {}", bundleId);
        return bundleLicenseService.getBundle(bundleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if a school has access to a specific game
     * GET /api/bundles/access/game/{gameId}?schoolId=xxx
     */
    @GetMapping("/access/game/{gameId}")
    public ResponseEntity<ApiResponse<Boolean>> checkGameAccess(
            @PathVariable String gameId,
            @RequestParam String schoolId) {
        logger.debug("Checking game access - school: {}, game: {}", schoolId, gameId);
        boolean hasAccess = bundleLicenseService.hasGameAccess(schoolId, gameId);
        return ResponseEntity.ok(ApiResponse.success(
                hasAccess ? "Access granted" : "Access denied",
                hasAccess
        ));
    }

    /**
     * Get all games accessible to a school
     * GET /api/bundles/access/games?schoolId=xxx
     */
    @GetMapping("/access/games")
    public ResponseEntity<Set<String>> getAccessibleGames(@RequestParam String schoolId) {
        logger.debug("Getting accessible games for school: {}", schoolId);
        Set<String> games = bundleLicenseService.getAccessibleGames(schoolId);
        return ResponseEntity.ok(games);
    }

    /**
     * Get licenses for a school
     * GET /api/bundles/licenses?schoolId=xxx
     */
    @GetMapping("/licenses")
    public ResponseEntity<List<SchoolLicenseEntity>> getSchoolLicenses(@RequestParam String schoolId) {
        logger.debug("Getting licenses for school: {}", schoolId);
        List<SchoolLicenseEntity> licenses = bundleLicenseService.getSchoolLicenses(schoolId);
        return ResponseEntity.ok(licenses);
    }

    /**
     * Get active licenses for a school
     * GET /api/bundles/licenses/active?schoolId=xxx
     */
    @GetMapping("/licenses/active")
    public ResponseEntity<List<SchoolLicenseEntity>> getActiveSchoolLicenses(@RequestParam String schoolId) {
        logger.debug("Getting active licenses for school: {}", schoolId);
        List<SchoolLicenseEntity> licenses = bundleLicenseService.getActiveSchoolLicenses(schoolId);
        return ResponseEntity.ok(licenses);
    }

    /**
     * Activate a license using a license key
     * POST /api/bundles/licenses/activate
     */
    @PostMapping("/licenses/activate")
    public ResponseEntity<ApiResponse<SchoolLicenseEntity>> activateLicense(
            @RequestParam String schoolId,
            @RequestParam String licenseKey,
            Authentication auth) {
        logger.info("Activating license - school: {}, key: {}, user: {}",
                schoolId, licenseKey, (auth != null ? auth.getName() : "anonymous"));
        try {
            SchoolLicenseEntity license = bundleLicenseService.activateLicense(schoolId, licenseKey);
            return ResponseEntity.ok(ApiResponse.success("License activated successfully", license));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create a new license (admin only)
     * POST /api/bundles/licenses/create
     */
    @PostMapping("/licenses/create")
    public ResponseEntity<ApiResponse<SchoolLicenseEntity>> createLicense(
            @RequestParam String schoolId,
            @RequestParam String bundleId,
            @RequestParam(defaultValue = "ANNUAL") String licenseType,
            @RequestParam(required = false) Integer maxDevices,
            @RequestParam(required = false) String purchasedBy,
            @RequestParam(required = false) String poNumber,
            Authentication auth) {
        logger.info("Creating license - school: {}, bundle: {}, type: {}, user: {}",
                schoolId, bundleId, licenseType, (auth != null ? auth.getName() : "anonymous"));
        try {
            SchoolLicenseEntity license = bundleLicenseService.createLicense(
                    schoolId, bundleId, licenseType, maxDevices, purchasedBy, poNumber);
            return ResponseEntity.ok(ApiResponse.success("License created successfully", license));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Suspend a license (admin only)
     * POST /api/bundles/licenses/{licenseId}/suspend
     */
    @PostMapping("/licenses/{licenseId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendLicense(
            @PathVariable Long licenseId,
            @RequestParam(required = false) String reason,
            Authentication auth) {
        logger.info("Suspending license {} - user: {}", licenseId, (auth != null ? auth.getName() : "anonymous"));
        try {
            bundleLicenseService.suspendLicense(licenseId, reason);
            return ResponseEntity.ok(ApiResponse.success("License suspended", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cancel a license (admin only)
     * POST /api/bundles/licenses/{licenseId}/cancel
     */
    @PostMapping("/licenses/{licenseId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelLicense(
            @PathVariable Long licenseId,
            @RequestParam(required = false) String reason,
            Authentication auth) {
        logger.info("Cancelling license {} - user: {}", licenseId, (auth != null ? auth.getName() : "anonymous"));
        try {
            bundleLicenseService.cancelLicense(licenseId, reason);
            return ResponseEntity.ok(ApiResponse.success("License cancelled", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get licenses expiring soon (for admin dashboard)
     * GET /api/bundles/licenses/expiring
     */
    @GetMapping("/licenses/expiring")
    public ResponseEntity<List<SchoolLicenseEntity>> getExpiringLicenses(Authentication auth) {
        logger.debug("Getting expiring licenses - user: {}", (auth != null ? auth.getName() : "anonymous"));
        List<SchoolLicenseEntity> licenses = bundleLicenseService.getLicensesExpiringSoon();
        return ResponseEntity.ok(licenses);
    }
}
