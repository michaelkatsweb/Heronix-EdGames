package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a bundle of games that can be purchased by schools.
 * Standard bundles are free and included with the platform.
 * Premium bundles require a license purchase.
 */
@Entity
@Table(name = "game_bundles")
public class GameBundleEntity {

    @Id
    @Column(name = "bundle_id", length = 50)
    private String bundleId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "bundle_type", nullable = false, length = 20)
    private String bundleType; // STANDARD, PREMIUM

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "subject_focus", length = 50)
    private String subjectFocus; // e.g., "STEM", "Language Arts", "All Subjects"

    @Column(name = "target_grades", length = 100)
    private String targetGrades; // JSON array

    @ElementCollection
    @CollectionTable(name = "bundle_games", joinColumns = @JoinColumn(name = "bundle_id"))
    @Column(name = "game_id")
    private Set<String> gameIds = new HashSet<>();

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBundleType() {
        return bundleType;
    }

    public void setBundleType(String bundleType) {
        this.bundleType = bundleType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSubjectFocus() {
        return subjectFocus;
    }

    public void setSubjectFocus(String subjectFocus) {
        this.subjectFocus = subjectFocus;
    }

    public String getTargetGrades() {
        return targetGrades;
    }

    public void setTargetGrades(String targetGrades) {
        this.targetGrades = targetGrades;
    }

    public Set<String> getGameIds() {
        return gameIds;
    }

    public void setGameIds(Set<String> gameIds) {
        this.gameIds = gameIds;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}
