package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.GameSessionEntity;
import com.heronix.edu.server.entity.GameSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for game session persistence.
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {

    /**
     * Find active sessions by teacher.
     */
    List<GameSessionEntity> findByTeacherIdAndStatusIn(String teacherId, List<GameSessionStatus> statuses);

    /**
     * Find all sessions created by a teacher.
     */
    List<GameSessionEntity> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    /**
     * Find session by code (session ID is the join code).
     */
    Optional<GameSessionEntity> findBySessionId(String sessionCode);

    /**
     * Find active or waiting sessions (not ended).
     */
    @Query("SELECT s FROM GameSessionEntity s WHERE s.status IN ('WAITING', 'ACTIVE', 'PAUSED')")
    List<GameSessionEntity> findActiveSessions();

    /**
     * Find sessions that ended within a time range.
     */
    @Query("SELECT s FROM GameSessionEntity s WHERE s.endedAt BETWEEN :start AND :end ORDER BY s.endedAt DESC")
    List<GameSessionEntity> findSessionsEndedBetween(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    /**
     * Count active sessions for a teacher.
     */
    long countByTeacherIdAndStatus(String teacherId, GameSessionStatus status);

    /**
     * Find recent sessions for a teacher (last 30 days).
     */
    @Query("SELECT s FROM GameSessionEntity s WHERE s.teacherId = :teacherId AND s.createdAt > :since ORDER BY s.createdAt DESC")
    List<GameSessionEntity> findRecentByTeacher(@Param("teacherId") String teacherId,
                                                 @Param("since") LocalDateTime since);
}
