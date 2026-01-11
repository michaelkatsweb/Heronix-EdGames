package com.heronix.edu.client.db.repository;

import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.entity.LocalGameScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for game score data
 * Handles offline score queue and sync status
 */
public class GameScoreRepository {
    private static final Logger logger = LoggerFactory.getLogger(GameScoreRepository.class);

    /**
     * Save a new game score
     */
    public void save(LocalGameScore score) {
        String sql = "INSERT INTO game_score (score_id, student_id, game_id, score, max_score, " +
                     "time_seconds, correct_answers, incorrect_answers, completion_percentage, " +
                     "completed, difficulty_level, played_at, device_id, synced, synced_at, " +
                     "sync_attempts, last_sync_error, metadata) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, score.getScoreId());
            pstmt.setString(2, score.getStudentId());
            pstmt.setString(3, score.getGameId());
            pstmt.setInt(4, score.getScore());
            pstmt.setInt(5, score.getMaxScore());
            setIntOrNull(pstmt, 6, score.getTimeSeconds());
            setIntOrNull(pstmt, 7, score.getCorrectAnswers());
            setIntOrNull(pstmt, 8, score.getIncorrectAnswers());
            setIntOrNull(pstmt, 9, score.getCompletionPercentage());
            pstmt.setBoolean(10, score.isCompleted());
            pstmt.setString(11, score.getDifficultyLevel());
            pstmt.setTimestamp(12, Timestamp.valueOf(score.getPlayedAt()));
            pstmt.setString(13, score.getDeviceId());
            pstmt.setBoolean(14, score.isSynced());
            pstmt.setTimestamp(15, toTimestamp(score.getSyncedAt()));
            pstmt.setInt(16, score.getSyncAttempts());
            pstmt.setString(17, score.getLastSyncError());
            pstmt.setString(18, score.getMetadata());

            pstmt.executeUpdate();
            logger.debug("Game score saved: {}", score.getScoreId());

        } catch (SQLException e) {
            logger.error("Error saving game score", e);
            throw new RuntimeException("Failed to save game score", e);
        }
    }

    /**
     * Find unsynced scores
     */
    public List<LocalGameScore> findUnsyncedScores() {
        String sql = "SELECT * FROM game_score WHERE synced = FALSE ORDER BY played_at";
        return findScoresBySql(sql);
    }

    /**
     * Find unsynced scores with limit
     */
    public List<LocalGameScore> findUnsyncedScores(int limit) {
        String sql = "SELECT * FROM game_score WHERE synced = FALSE ORDER BY played_at LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<LocalGameScore> scores = new ArrayList<>();
                while (rs.next()) {
                    scores.add(mapResultSetToScore(rs));
                }
                return scores;
            }

        } catch (SQLException e) {
            logger.error("Error finding unsynced scores", e);
            throw new RuntimeException("Failed to find unsynced scores", e);
        }
    }

    /**
     * Find score by ID
     */
    public Optional<LocalGameScore> findById(String scoreId) {
        String sql = "SELECT * FROM game_score WHERE score_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, scoreId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToScore(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding score by ID", e);
            throw new RuntimeException("Failed to find score", e);
        }
    }

    /**
     * Mark a score as synced
     */
    public void markAsSynced(String scoreId) {
        String sql = "UPDATE game_score SET synced = TRUE, synced_at = ? WHERE score_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, scoreId);

            pstmt.executeUpdate();
            logger.debug("Score marked as synced: {}", scoreId);

        } catch (SQLException e) {
            logger.error("Error marking score as synced", e);
            throw new RuntimeException("Failed to mark score as synced", e);
        }
    }

    /**
     * Increment sync attempt counter and record error
     */
    public void incrementSyncAttempt(String scoreId, String errorMessage) {
        String sql = "UPDATE game_score SET sync_attempts = sync_attempts + 1, " +
                     "last_sync_error = ? WHERE score_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, errorMessage);
            pstmt.setString(2, scoreId);

            pstmt.executeUpdate();
            logger.debug("Sync attempt incremented for score: {}", scoreId);

        } catch (SQLException e) {
            logger.error("Error incrementing sync attempt", e);
            throw new RuntimeException("Failed to increment sync attempt", e);
        }
    }

    /**
     * Get count of unsynced scores
     */
    public int countUnsyncedScores() {
        String sql = "SELECT COUNT(*) FROM game_score WHERE synced = FALSE";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error counting unsynced scores", e);
            throw new RuntimeException("Failed to count unsynced scores", e);
        }
    }

    /**
     * Find scores by SQL query
     */
    private List<LocalGameScore> findScoresBySql(String sql) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<LocalGameScore> scores = new ArrayList<>();
            while (rs.next()) {
                scores.add(mapResultSetToScore(rs));
            }
            return scores;

        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    /**
     * Map ResultSet to LocalGameScore entity
     */
    private LocalGameScore mapResultSetToScore(ResultSet rs) throws SQLException {
        LocalGameScore score = new LocalGameScore();
        score.setScoreId(rs.getString("score_id"));
        score.setStudentId(rs.getString("student_id"));
        score.setGameId(rs.getString("game_id"));
        score.setScore(rs.getInt("score"));
        score.setMaxScore(rs.getInt("max_score"));
        score.setTimeSeconds(getIntOrNull(rs, "time_seconds"));
        score.setCorrectAnswers(getIntOrNull(rs, "correct_answers"));
        score.setIncorrectAnswers(getIntOrNull(rs, "incorrect_answers"));
        score.setCompletionPercentage(getIntOrNull(rs, "completion_percentage"));
        score.setCompleted(rs.getBoolean("completed"));
        score.setDifficultyLevel(rs.getString("difficulty_level"));
        score.setPlayedAt(rs.getTimestamp("played_at").toLocalDateTime());
        score.setDeviceId(rs.getString("device_id"));
        score.setSynced(rs.getBoolean("synced"));
        score.setSyncedAt(toLocalDateTime(rs.getTimestamp("synced_at")));
        score.setSyncAttempts(rs.getInt("sync_attempts"));
        score.setLastSyncError(rs.getString("last_sync_error"));
        score.setMetadata(rs.getString("metadata"));
        return score;
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private void setIntOrNull(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (value != null) {
            pstmt.setInt(index, value);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }

    private Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
