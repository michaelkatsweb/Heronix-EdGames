package com.heronix.edu.server.service;

import com.heronix.edu.server.entity.GameBundleEntity;
import com.heronix.edu.server.entity.SchoolLicenseEntity;
import com.heronix.edu.server.repository.GameBundleRepository;
import com.heronix.edu.server.repository.SchoolLicenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing game bundles and school licenses.
 * Handles license validation, activation, and expiration.
 */
@Service
public class BundleLicenseService {

    private static final Logger logger = LoggerFactory.getLogger(BundleLicenseService.class);

    private final GameBundleRepository bundleRepository;
    private final SchoolLicenseRepository licenseRepository;

    public BundleLicenseService(GameBundleRepository bundleRepository, SchoolLicenseRepository licenseRepository) {
        this.bundleRepository = bundleRepository;
        this.licenseRepository = licenseRepository;
    }

    // ==================== Bundle Management ====================

    /**
     * Get all available bundles
     */
    public List<GameBundleEntity> getAllBundles() {
        return bundleRepository.findByActiveTrue();
    }

    /**
     * Get standard (free) bundles
     */
    public List<GameBundleEntity> getStandardBundles() {
        return bundleRepository.findByBundleTypeAndActiveTrue("STANDARD");
    }

    /**
     * Get premium bundles
     */
    public List<GameBundleEntity> getPremiumBundles() {
        return bundleRepository.findByBundleTypeAndActiveTrue("PREMIUM");
    }

    /**
     * Get bundle by ID
     */
    public Optional<GameBundleEntity> getBundle(String bundleId) {
        return bundleRepository.findById(bundleId);
    }

    /**
     * Create or update a bundle
     */
    @Transactional
    public GameBundleEntity saveBundle(GameBundleEntity bundle) {
        logger.info("Saving bundle: {}", bundle.getBundleId());
        return bundleRepository.save(bundle);
    }

    // ==================== License Management ====================

    /**
     * Get all licenses for a school
     */
    public List<SchoolLicenseEntity> getSchoolLicenses(String schoolId) {
        return licenseRepository.findBySchoolId(schoolId);
    }

    /**
     * Get active licenses for a school
     */
    public List<SchoolLicenseEntity> getActiveSchoolLicenses(String schoolId) {
        return licenseRepository.findBySchoolIdAndStatus(schoolId, "ACTIVE");
    }

    /**
     * Check if a school has access to a specific game
     */
    public boolean hasGameAccess(String schoolId, String gameId) {
        // First check standard bundles (always accessible)
        List<GameBundleEntity> standardBundles = bundleRepository.findByBundleTypeAndActiveTrue("STANDARD");
        for (GameBundleEntity bundle : standardBundles) {
            if (bundle.getGameIds().contains(gameId)) {
                return true;
            }
        }

        // Check premium bundles with valid licenses
        List<GameBundleEntity> premiumBundles = bundleRepository.findBundlesContainingGame(gameId);
        for (GameBundleEntity bundle : premiumBundles) {
            if ("PREMIUM".equals(bundle.getBundleType())) {
                if (licenseRepository.hasValidLicense(schoolId, bundle.getBundleId(), LocalDate.now())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get all games accessible to a school
     */
    public Set<String> getAccessibleGames(String schoolId) {
        Set<String> accessibleGames = new HashSet<>();

        // Add games from standard bundles
        List<GameBundleEntity> standardBundles = bundleRepository.findByBundleTypeAndActiveTrue("STANDARD");
        for (GameBundleEntity bundle : standardBundles) {
            accessibleGames.addAll(bundle.getGameIds());
        }

        // Add games from licensed premium bundles
        List<SchoolLicenseEntity> activeLicenses = licenseRepository.findBySchoolIdAndStatus(schoolId, "ACTIVE");
        for (SchoolLicenseEntity license : activeLicenses) {
            if (license.isValid()) {
                Optional<GameBundleEntity> bundle = bundleRepository.findById(license.getBundleId());
                bundle.ifPresent(b -> accessibleGames.addAll(b.getGameIds()));
            }
        }

        return accessibleGames;
    }

    /**
     * Activate a license using a license key
     */
    @Transactional
    public SchoolLicenseEntity activateLicense(String schoolId, String licenseKey) {
        Optional<SchoolLicenseEntity> existingLicense = licenseRepository.findByLicenseKey(licenseKey);

        if (existingLicense.isEmpty()) {
            throw new IllegalArgumentException("Invalid license key");
        }

        SchoolLicenseEntity license = existingLicense.get();

        if (!license.getSchoolId().equals(schoolId)) {
            throw new IllegalArgumentException("License key is not valid for this school");
        }

        if (!"PENDING".equals(license.getStatus())) {
            throw new IllegalArgumentException("License has already been activated or is not available");
        }

        license.setStatus("ACTIVE");
        license.setStartDate(LocalDate.now());

        // Set end date based on license type
        if ("ANNUAL".equals(license.getLicenseType())) {
            license.setEndDate(LocalDate.now().plusYears(1));
        } else if ("TRIAL".equals(license.getLicenseType())) {
            license.setEndDate(LocalDate.now().plusDays(30));
        }
        // PERPETUAL licenses have no end date

        logger.info("Activated license {} for school {}", licenseKey, schoolId);
        return licenseRepository.save(license);
    }

    /**
     * Create a new license for a school
     */
    @Transactional
    public SchoolLicenseEntity createLicense(String schoolId, String bundleId, String licenseType,
                                              Integer maxDevices, String purchasedBy, String poNumber) {
        // Check if bundle exists
        Optional<GameBundleEntity> bundle = bundleRepository.findById(bundleId);
        if (bundle.isEmpty()) {
            throw new IllegalArgumentException("Bundle not found: " + bundleId);
        }

        // Generate unique license key
        String licenseKey = generateLicenseKey();

        SchoolLicenseEntity license = new SchoolLicenseEntity();
        license.setSchoolId(schoolId);
        license.setBundleId(bundleId);
        license.setLicenseKey(licenseKey);
        license.setLicenseType(licenseType);
        license.setStartDate(LocalDate.now());
        license.setMaxDevices(maxDevices);
        license.setPurchasedBy(purchasedBy);
        license.setPurchaseOrderNumber(poNumber);
        license.setStatus("ACTIVE");

        // Set end date based on license type
        switch (licenseType) {
            case "TRIAL":
                license.setEndDate(LocalDate.now().plusDays(30));
                break;
            case "ANNUAL":
                license.setEndDate(LocalDate.now().plusYears(1));
                break;
            case "PERPETUAL":
                license.setEndDate(null);
                break;
            default:
                license.setEndDate(LocalDate.now().plusYears(1));
        }

        logger.info("Created {} license for school {} - bundle: {}", licenseType, schoolId, bundleId);
        return licenseRepository.save(license);
    }

    /**
     * Generate a unique license key
     */
    private String generateLicenseKey() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder key = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            if (i > 0) key.append("-");
            for (int j = 0; j < 4; j++) {
                key.append(chars.charAt(random.nextInt(chars.length())));
            }
        }

        return key.toString();
    }

    /**
     * Suspend a license
     */
    @Transactional
    public void suspendLicense(Long licenseId, String reason) {
        SchoolLicenseEntity license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found"));

        license.setStatus("SUSPENDED");
        license.setNotes(reason);
        licenseRepository.save(license);

        logger.info("Suspended license {} - reason: {}", licenseId, reason);
    }

    /**
     * Cancel a license
     */
    @Transactional
    public void cancelLicense(Long licenseId, String reason) {
        SchoolLicenseEntity license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found"));

        license.setStatus("CANCELLED");
        license.setNotes(reason);
        licenseRepository.save(license);

        logger.info("Cancelled license {} - reason: {}", licenseId, reason);
    }

    // ==================== Scheduled Tasks ====================

    /**
     * Check and expire licenses daily
     */
    @Scheduled(cron = "0 0 1 * * *") // Run at 1 AM daily
    @Transactional
    public void expireLicenses() {
        logger.info("Running license expiration check");
        List<SchoolLicenseEntity> expiredLicenses = licenseRepository.findExpiredActiveLicenses(LocalDate.now());

        for (SchoolLicenseEntity license : expiredLicenses) {
            license.setStatus("EXPIRED");
            licenseRepository.save(license);
            logger.info("Expired license {} for school {}", license.getLicenseId(), license.getSchoolId());
        }

        logger.info("Expired {} licenses", expiredLicenses.size());
    }

    /**
     * Get licenses expiring within the next 30 days (for notifications)
     */
    public List<SchoolLicenseEntity> getLicensesExpiringSoon() {
        return licenseRepository.findLicensesExpiringSoon(LocalDate.now().plusDays(30));
    }
}
