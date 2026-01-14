package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.GamePlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for game player persistence.
 */
@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, String> {

    /**
     * Find all players in a session.
     */
    @Query("SELECT p FROM GamePlayerEntity p WHERE p.session.sessionId = :sessionId ORDER BY p.credits DESC")
    List<GamePlayerEntity> findBySessionIdOrderByCreditsDesc(@Param("sessionId") String sessionId);

    /**
     * Find a specific player in a session.
     */
    @Query("SELECT p FROM GamePlayerEntity p WHERE p.session.sessionId = :sessionId AND p.studentId = :studentId")
    Optional<GamePlayerEntity> findBySessionIdAndStudentId(@Param("sessionId") String sessionId,
                                                            @Param("studentId") String studentId);

    /**
     * Find player by player ID.
     */
    Optional<GamePlayerEntity> findByPlayerId(String playerId);

    /**
     * Count players in a session.
     */
    @Query("SELECT COUNT(p) FROM GamePlayerEntity p WHERE p.session.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);

    /**
     * Count connected players in a session.
     */
    @Query("SELECT COUNT(p) FROM GamePlayerEntity p WHERE p.session.sessionId = :sessionId AND p.connected = true")
    long countConnectedBySessionId(@Param("sessionId") String sessionId);

    /**
     * Get leaderboard (top players by credits).
     */
    @Query("SELECT p FROM GamePlayerEntity p WHERE p.session.sessionId = :sessionId ORDER BY p.credits DESC")
    List<GamePlayerEntity> getLeaderboard(@Param("sessionId") String sessionId);

    /**
     * Find all game history for a student.
     */
    List<GamePlayerEntity> findByStudentIdOrderByJoinedAtDesc(String studentId);

    /**
     * Calculate total stats for a student across all games.
     */
    @Query("SELECT SUM(p.correctAnswers), SUM(p.incorrectAnswers), SUM(p.successfulHacks), SUM(p.credits) " +
           "FROM GamePlayerEntity p WHERE p.studentId = :studentId")
    Object[] getStudentTotalStats(@Param("studentId") String studentId);
}
