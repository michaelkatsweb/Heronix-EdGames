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

    // Analytics queries for play time reporting
    @Query("SELECT SUM(s.timeSeconds) FROM GameScoreEntity s WHERE s.studentId = :studentId")
    Long sumTimeSecondsByStudentId(@Param("studentId") String studentId);

    @Query("SELECT SUM(s.timeSeconds) FROM GameScoreEntity s WHERE s.studentId = :studentId AND s.playedAt >= :start AND s.playedAt <= :end")
    Long sumTimeSecondsByStudentIdAndDateRange(
        @Param("studentId") String studentId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT s.gameId, SUM(s.timeSeconds), COUNT(s), AVG(s.score * 100.0 / s.maxScore), MAX(s.score) " +
           "FROM GameScoreEntity s WHERE s.studentId = :studentId " +
           "GROUP BY s.gameId")
    List<Object[]> getGameBreakdownByStudentId(@Param("studentId") String studentId);

    @Query("SELECT s.gameId, SUM(s.timeSeconds), COUNT(s), AVG(s.score * 100.0 / s.maxScore), MAX(s.score) " +
           "FROM GameScoreEntity s WHERE s.studentId = :studentId AND s.playedAt >= :start AND s.playedAt <= :end " +
           "GROUP BY s.gameId")
    List<Object[]> getGameBreakdownByStudentIdAndDateRange(
        @Param("studentId") String studentId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT s.studentId FROM GameScoreEntity s WHERE s.playedAt >= :start AND s.playedAt <= :end")
    List<String> findActiveStudentIds(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.deviceId, SUM(s.timeSeconds), COUNT(s) " +
           "FROM GameScoreEntity s WHERE s.deviceId IS NOT NULL " +
           "GROUP BY s.deviceId")
    List<Object[]> getPlayTimeByDevice();

    @Query("SELECT MIN(s.playedAt) FROM GameScoreEntity s WHERE s.studentId = :studentId")
    LocalDateTime findFirstPlayDateByStudentId(@Param("studentId") String studentId);

    @Query("SELECT MAX(s.playedAt) FROM GameScoreEntity s WHERE s.studentId = :studentId")
    LocalDateTime findLastPlayDateByStudentId(@Param("studentId") String studentId);

    @Query("SELECT s.gameId, COUNT(DISTINCT s.studentId), SUM(s.timeSeconds), COUNT(s), AVG(s.score * 100.0 / s.maxScore) " +
           "FROM GameScoreEntity s WHERE s.playedAt >= :start AND s.playedAt <= :end " +
           "GROUP BY s.gameId")
    List<Object[]> getGameUsageStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<GameScoreEntity> findByDeviceId(String deviceId);
}
