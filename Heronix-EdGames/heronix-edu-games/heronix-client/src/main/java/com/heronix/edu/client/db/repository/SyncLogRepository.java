package com.heronix.edu.client.db.repository;

import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.entity.SyncLog;
import com.heronix.edu.client.db.entity.SyncLog.SyncType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for sync log data.
 * Handles sync history and audit trail.
 */
public class SyncLogRepository {
    private static final Logger logger = LoggerFactory.getLogger(SyncLogRepository.class);

    /**
     * Save a new sync log entry
     */
    public void save(SyncLog log) {
        String sql = "INSERT INTO sync_log (sync_started_at, sync_completed_at, sync_type, " +
                     "scores_uploaded, scores_failed, bytes_transferred, success, error_message, " +
                     "conflicts_detected, conflicts_resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(log.getSyncStartedAt()));
            setTimestamp(pstmt, 2, log.getSyncCompletedAt());
            pstmt.setString(3, log.getSyncType().name());
            pstmt.setInt(4, log.getScoresUploaded());
            pstmt.setInt(5, log.getScoresFailed());
            pstmt.setLong(6, log.getBytesTransferred());
            pstmt.setBoolean(7, log.isSuccess());
            pstmt.setString(8, log.getErrorMessage());
            pstmt.setInt(9, log.getConflictsDetected());
            pstmt.setInt(10, log.getConflictsResolved());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getLong(1));
                }
            }
            logger.debug("Sync log saved: {}", log);

        } catch (SQLException e) {
            logger.error("Error saving sync log", e);
            throw new RuntimeException("Failed to save sync log", e);
        }
    }

    /**
     * Update an existing sync log (after sync completes)
     */
    public void update(SyncLog log) {
        String sql = "UPDATE sync_log SET sync_completed_at = ?, scores_uploaded = ?, " +
                     "scores_failed = ?, bytes_transferred = ?, success = ?, error_message = ?, " +
                     "conflicts_detected = ?, conflicts_resolved = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setTimestamp(pstmt, 1, log.getSyncCompletedAt());
            pstmt.setInt(2, log.getScoresUploaded());
            pstmt.setInt(3, log.getScoresFailed());
            pstmt.setLong(4, log.getBytesTransferred());
            pstmt.setBoolean(5, log.isSuccess());
            pstmt.setString(6, log.getErrorMessage());
            pstmt.setInt(7, log.getConflictsDetected());
            pstmt.setInt(8, log.getConflictsResolved());
            pstmt.setLong(9, log.getId());

            pstmt.executeUpdate();
            logger.debug("Sync log updated: {}", log);

        } catch (SQLException e) {
            logger.error("Error updating sync log", e);
            throw new RuntimeException("Failed to update sync log", e);
        }
    }

    /**
     * Find the most recent sync log
     */
    public Optional<SyncLog> findLatest() {
        String sql = "SELECT * FROM sync_log ORDER BY sync_started_at DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding latest sync log", e);
            throw new RuntimeException("Failed to find latest sync log", e);
        }
    }

    /**
     * Find last successful sync log
     */
    public Optional<SyncLog> findLastSuccessful() {
        String sql = "SELECT * FROM sync_log WHERE success = TRUE ORDER BY sync_completed_at DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding last successful sync", e);
            throw new RuntimeException("Failed to find last successful sync", e);
        }
    }

    /**
     * Find recent sync logs
     */
    public List<SyncLog> findRecent(int limit) {
        String sql = "SELECT * FROM sync_log ORDER BY sync_started_at DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<SyncLog> logs = new ArrayList<>();
                while (rs.next()) {
                    logs.add(mapResultSet(rs));
                }
                return logs;
            }

        } catch (SQLException e) {
            logger.error("Error finding recent sync logs", e);
            throw new RuntimeException("Failed to find recent sync logs", e);
        }
    }

    /**
     * Find sync logs by date range
     */
    public List<SyncLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT * FROM sync_log WHERE sync_started_at BETWEEN ? AND ? ORDER BY sync_started_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(from));
            pstmt.setTimestamp(2, Timestamp.valueOf(to));

            try (ResultSet rs = pstmt.executeQuery()) {
                List<SyncLog> logs = new ArrayList<>();
                while (rs.next()) {
                    logs.add(mapResultSet(rs));
                }
                return logs;
            }

        } catch (SQLException e) {
            logger.error("Error finding sync logs by date range", e);
            throw new RuntimeException("Failed to find sync logs", e);
        }
    }

    /**
     * Get sync statistics
     */
    public SyncStats getStats() {
        String sql = "SELECT COUNT(*) as total_syncs, " +
                     "SUM(CASE WHEN success THEN 1 ELSE 0 END) as successful_syncs, " +
                     "SUM(scores_uploaded) as total_scores_uploaded, " +
                     "SUM(bytes_transferred) as total_bytes " +
                     "FROM sync_log";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new SyncStats(
                    rs.getInt("total_syncs"),
                    rs.getInt("successful_syncs"),
                    rs.getLong("total_scores_uploaded"),
                    rs.getLong("total_bytes")
                );
            }
            return new SyncStats(0, 0, 0, 0);

        } catch (SQLException e) {
            logger.error("Error getting sync stats", e);
            throw new RuntimeException("Failed to get sync stats", e);
        }
    }

    /**
     * Delete old sync logs
     */
    public int deleteOlderThan(int daysOld) {
        String sql = "DELETE FROM sync_log WHERE sync_started_at < ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusDays(daysOld)));

            int deleted = pstmt.executeUpdate();
            logger.info("Deleted {} old sync logs", deleted);
            return deleted;

        } catch (SQLException e) {
            logger.error("Error deleting old sync logs", e);
            throw new RuntimeException("Failed to delete old sync logs", e);
        }
    }

    private SyncLog mapResultSet(ResultSet rs) throws SQLException {
        SyncLog log = new SyncLog();
        log.setId(rs.getLong("id"));
        log.setSyncStartedAt(rs.getTimestamp("sync_started_at").toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("sync_completed_at");
        log.setSyncCompletedAt(completedAt != null ? completedAt.toLocalDateTime() : null);
        log.setSyncType(SyncType.valueOf(rs.getString("sync_type")));
        log.setScoresUploaded(rs.getInt("scores_uploaded"));
        log.setScoresFailed(rs.getInt("scores_failed"));
        log.setBytesTransferred(rs.getLong("bytes_transferred"));
        log.setSuccess(rs.getBoolean("success"));
        log.setErrorMessage(rs.getString("error_message"));
        log.setConflictsDetected(rs.getInt("conflicts_detected"));
        log.setConflictsResolved(rs.getInt("conflicts_resolved"));
        return log;
    }

    private void setTimestamp(PreparedStatement pstmt, int index, LocalDateTime dateTime) throws SQLException {
        if (dateTime != null) {
            pstmt.setTimestamp(index, Timestamp.valueOf(dateTime));
        } else {
            pstmt.setNull(index, Types.TIMESTAMP);
        }
    }

    /**
     * Stats summary for display
     */
    public static class SyncStats {
        public final int totalSyncs;
        public final int successfulSyncs;
        public final long totalScoresUploaded;
        public final long totalBytesTransferred;

        public SyncStats(int totalSyncs, int successfulSyncs, long totalScoresUploaded, long totalBytesTransferred) {
            this.totalSyncs = totalSyncs;
            this.successfulSyncs = successfulSyncs;
            this.totalScoresUploaded = totalScoresUploaded;
            this.totalBytesTransferred = totalBytesTransferred;
        }

        public double getSuccessRate() {
            return totalSyncs > 0 ? (double) successfulSyncs / totalSyncs * 100 : 0;
        }
    }
}
