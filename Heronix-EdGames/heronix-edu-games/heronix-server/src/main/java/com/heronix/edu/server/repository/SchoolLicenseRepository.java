package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.SchoolLicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SchoolLicense entities
 */
@Repository
public interface SchoolLicenseRepository extends JpaRepository<SchoolLicenseEntity, Long> {

    /**
     * Find all licenses for a school
     */
    List<SchoolLicenseEntity> findBySchoolId(String schoolId);

    /**
     * Find active licenses for a school
     */
    List<SchoolLicenseEntity> findBySchoolIdAndStatus(String schoolId, String status);

    /**
     * Find license by school and bundle
     */
    Optional<SchoolLicenseEntity> findBySchoolIdAndBundleIdAndStatus(String schoolId, String bundleId, String status);

    /**
     * Find license by license key
     */
    Optional<SchoolLicenseEntity> findByLicenseKey(String licenseKey);

    /**
     * Find all active licenses for a specific bundle
     */
    List<SchoolLicenseEntity> findByBundleIdAndStatus(String bundleId, String status);

    /**
     * Find licenses expiring soon (within given days)
     */
    @Query("SELECT l FROM SchoolLicenseEntity l WHERE l.status = 'ACTIVE' AND l.endDate IS NOT NULL AND l.endDate <= :expiryDate")
    List<SchoolLicenseEntity> findLicensesExpiringSoon(LocalDate expiryDate);

    /**
     * Find all expired licenses that need status update
     */
    @Query("SELECT l FROM SchoolLicenseEntity l WHERE l.status = 'ACTIVE' AND l.endDate IS NOT NULL AND l.endDate < :today")
    List<SchoolLicenseEntity> findExpiredActiveLicenses(LocalDate today);

    /**
     * Check if school has valid license for bundle
     */
    @Query("SELECT COUNT(l) > 0 FROM SchoolLicenseEntity l WHERE l.schoolId = :schoolId AND l.bundleId = :bundleId AND l.status = 'ACTIVE' AND l.startDate <= :today AND (l.endDate IS NULL OR l.endDate >= :today)")
    boolean hasValidLicense(String schoolId, String bundleId, LocalDate today);
}
