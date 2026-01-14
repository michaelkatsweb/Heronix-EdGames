package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a school's license to use a game bundle.
 * Standard bundles are automatically licensed to all schools.
 * Premium bundles require an active license with valid dates.
 */
@Entity
@Table(name = "school_licenses")
public class SchoolLicenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "school_id", nullable = false, length = 50)
    private String schoolId;

    @Column(name = "bundle_id", nullable = false, length = 50)
    private String bundleId;

    @Column(name = "license_key", unique = true, length = 100)
    private String licenseKey;

    @Column(name = "license_type", nullable = false, length = 20)
    private String licenseType; // STANDARD, TRIAL, ANNUAL, PERPETUAL

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate; // null for perpetual licenses

    @Column(name = "max_devices")
    private Integer maxDevices; // null for unlimited

    @Column(name = "active_devices")
    private Integer activeDevices = 0;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, SUSPENDED, CANCELLED

    @Column(name = "purchased_by", length = 100)
    private String purchasedBy;

    @Column(name = "purchase_order_number", length = 50)
    private String purchaseOrderNumber;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Long licenseId) {
        this.licenseId = licenseId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getMaxDevices() {
        return maxDevices;
    }

    public void setMaxDevices(Integer maxDevices) {
        this.maxDevices = maxDevices;
    }

    public Integer getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(Integer activeDevices) {
        this.activeDevices = activeDevices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurchasedBy() {
        return purchasedBy;
    }

    public void setPurchasedBy(String purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this license is currently valid
     */
    public boolean isValid() {
        if (!"ACTIVE".equals(status)) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    /**
     * Check if more devices can be activated under this license
     */
    public boolean canActivateDevice() {
        if (!isValid()) {
            return false;
        }
        if (maxDevices == null) {
            return true; // Unlimited
        }
        return activeDevices < maxDevices;
    }
}
