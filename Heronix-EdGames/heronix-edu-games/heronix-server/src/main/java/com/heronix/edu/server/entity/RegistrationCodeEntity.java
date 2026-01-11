package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity for Registration Codes.
 * Maps to the 'registration_codes' table.
 * Teachers generate these codes for students to register devices.
 */
@Entity
@Table(name = "registration_codes", indexes = {
    @Index(name = "idx_code", columnList = "code", unique = true)
})
public class RegistrationCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "teacher_id", length = 50)
    private String teacherId;

    @Column(name = "class_id", length = 50)
    private String classId;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "times_used", nullable = false)
    private Integer timesUsed = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public RegistrationCodeEntity() {
    }

    public RegistrationCodeEntity(String code, String teacherId, Integer maxUses, LocalDateTime expiresAt) {
        this.code = code;
        this.teacherId = teacherId;
        this.maxUses = maxUses;
        this.expiresAt = expiresAt;
        this.timesUsed = 0;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Business Methods
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        if (maxUses != null && timesUsed >= maxUses) return false;
        return true;
    }

    public void incrementUses() {
        this.timesUsed++;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Integer getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(Integer timesUsed) {
        this.timesUsed = timesUsed;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationCodeEntity that = (RegistrationCodeEntity) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "RegistrationCodeEntity{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", timesUsed=" + timesUsed +
                ", maxUses=" + maxUses +
                ", active=" + active +
                '}';
    }
}
