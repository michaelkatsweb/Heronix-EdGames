package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.GameScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GameScore entities.
 * Provides database access methods for game scores and progress.
 */
@Repository
public interface GameScoreRepository extends JpaRepository<GameScoreEntity, String> {

    Optional<GameScoreEntity> findByScoreId(String scoreId);

    List<GameScoreEntity> findByStudentId(String studentId);

    List<GameScoreEntity> findByStudentIdAndGameId(String studentId, String gameId);

    List<GameScoreEntity> findBySyncedFalse();

    @Query("SELECT s FROM GameScoreEntity s WHERE s.playedAt > :since")
    List<GameScoreEntity> findScoresSince(@Param("since") LocalDateTime since);

    @Query("SELECT s FROM GameScoreEntity s WHERE s.studentId = :studentId AND s.playedAt >= :start AND s.playedAt <= :end")
    List<GameScoreEntity> findByStudentIdAndDateRange(
        @Param("studentId") String studentId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    long countByStudentId(String studentId);

    long countByGameId(String gameId);
}
