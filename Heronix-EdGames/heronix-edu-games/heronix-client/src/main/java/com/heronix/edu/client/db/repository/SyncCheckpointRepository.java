package com.heronix.edu.client.db.repository;

import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.entity.SyncCheckpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for sync checkpoint data.
 * Tracks delta sync progress for efficient incremental syncing.
 */
public class SyncCheckpointRepository {
    private static final Logger logger = LoggerFactory.getLogger(SyncCheckpointRepository.class);

    /**
     * Get checkpoint for an entity type, creating if not exists
     */
    public SyncCheckpoint getOrCreate(String entityType) {
        Optional<SyncCheckpoint> existing = findById(entityType);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new checkpoint
        SyncCheckpoint checkpoint = new SyncCheckpoint(entityType);
        save(checkpoint);
        return checkpoint;
    }

    /**
     * Find checkpoint by entity type
     */
    public Optional<SyncCheckpoint> findById(String entityType) {
        String sql = "SELECT * FROM sync_checkpoint WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entityType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding checkpoint", e);
            throw new RuntimeException("Failed to find checkpoint", e);
        }
    }

    /**
     * Save a new checkpoint
     */
    public void save(SyncCheckpoint checkpoint) {
        String sql = "INSERT INTO sync_checkpoint (id, last_sync_timestamp, last_sync_version, " +
                     "server_sequence_id, updated_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, checkpoint.getId());
            setTimestamp(pstmt, 2, checkpoint.getLastSyncTimestamp());
            pstmt.setLong(3, checkpoint.getLastSyncVersion());
            pstmt.setString(4, checkpoint.getServerSequenceId());
            pstmt.setTimestamp(5, Timestamp.valueOf(checkpoint.getUpdatedAt()));

            pstmt.executeUpdate();
            logger.debug("Checkpoint saved: {}", checkpoint);

        } catch (SQLException e) {
            logger.error("Error saving checkpoint", e);
            throw new RuntimeException("Failed to save checkpoint", e);
        }
    }

    /**
     * Update an existing checkpoint
     */
    public void update(SyncCheckpoint checkpoint) {
        String sql = "UPDATE sync_checkpoint SET last_sync_timestamp = ?, last_sync_version = ?, " +
                     "server_sequence_id = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setTimestamp(pstmt, 1, checkpoint.getLastSyncTimestamp());
            pstmt.setLong(2, checkpoint.getLastSyncVersion());
            pstmt.setString(3, checkpoint.getServerSequenceId());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(5, checkpoint.getId());

            pstmt.executeUpdate();
            logger.debug("Checkpoint updated: {}", checkpoint);

        } catch (SQLException e) {
            logger.error("Error updating checkpoint", e);
            throw new RuntimeException("Failed to update checkpoint", e);
        }
    }

    /**
     * Update checkpoint after successful sync
     */
    public void updateAfterSync(String entityType, LocalDateTime timestamp, long version, String serverSequenceId) {
        SyncCheckpoint checkpoint = getOrCreate(entityType);
        checkpoint.updateCheckpoint(timestamp, version, serverSequenceId);
        update(checkpoint);
    }

    /**
     * Reset checkpoint (force full sync next time)
     */
    public void reset(String entityType) {
        String sql = "UPDATE sync_checkpoint SET last_sync_timestamp = NULL, last_sync_version = 0, " +
                     "server_sequence_id = NULL, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, entityType);

            pstmt.executeUpdate();
            logger.info("Checkpoint reset for entity type: {}", entityType);

        } catch (SQLException e) {
            logger.error("Error resetting checkpoint", e);
            throw new RuntimeException("Failed to reset checkpoint", e);
        }
    }

    /**
     * Delete checkpoint
     */
    public void delete(String entityType) {
        String sql = "DELETE FROM sync_checkpoint WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entityType);
            pstmt.executeUpdate();
            logger.debug("Checkpoint deleted: {}", entityType);

        } catch (SQLException e) {
            logger.error("Error deleting checkpoint", e);
            throw new RuntimeException("Failed to delete checkpoint", e);
        }
    }

    private SyncCheckpoint mapResultSet(ResultSet rs) throws SQLException {
        SyncCheckpoint checkpoint = new SyncCheckpoint();
        checkpoint.setId(rs.getString("id"));
        Timestamp lastSync = rs.getTimestamp("last_sync_timestamp");
        checkpoint.setLastSyncTimestamp(lastSync != null ? lastSync.toLocalDateTime() : null);
        checkpoint.setLastSyncVersion(rs.getLong("last_sync_version"));
        checkpoint.setServerSequenceId(rs.getString("server_sequence_id"));
        checkpoint.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return checkpoint;
    }

    private void setTimestamp(PreparedStatement pstmt, int index, LocalDateTime dateTime) throws SQLException {
        if (dateTime != null) {
            pstmt.setTimestamp(index, Timestamp.valueOf(dateTime));
        } else {
            pstmt.setNull(index, Types.TIMESTAMP);
        }
    }
}
