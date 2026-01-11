package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entities.
 * Provides database access methods for FERPA-compliant audit logging.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByUserId(String userId);

    List<AuditLogEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLogEntity> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<AuditLogEntity> findByActionContaining(String action);

    long countByResult(String result);
}
