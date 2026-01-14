package com.heronix.edu.client.db.repository;

import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.entity.SyncConflict;
import com.heronix.edu.client.db.entity.SyncConflict.ConflictType;
import com.heronix.edu.client.db.entity.SyncConflict.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for sync conflict data.
 * Handles conflict tracking and resolution status.
 */
public class SyncConflictRepository {
    private static final Logger logger = LoggerFactory.getLogger(SyncConflictRepository.class);

    /**
     * Save a new conflict
     */
    public void save(SyncConflict conflict) {
        String sql = "INSERT INTO sync_conflict (entity_type, entity_id, field_name, " +
                     "local_value, server_value, local_version, server_version, " +
                     "local_timestamp, server_timestamp, conflict_type, resolution, " +
                     "resolved_at, resolved_by, created_at, metadata) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, conflict.getEntityType());
            pstmt.setString(2, conflict.getEntityId());
            pstmt.setString(3, conflict.getFieldName());
            pstmt.setString(4, conflict.getLocalValue());
            pstmt.setString(5, conflict.getServerValue());
            setIntOrNull(pstmt, 6, conflict.getLocalVersion());
            setIntOrNull(pstmt, 7, conflict.getServerVersion());
            setTimestamp(pstmt, 8, conflict.getLocalTimestamp());
            setTimestamp(pstmt, 9, conflict.getServerTimestamp());
            pstmt.setString(10, conflict.getConflictType().name());
            pstmt.setString(11, conflict.getResolution() != null ? conflict.getResolution().name() : "PENDING");
            setTimestamp(pstmt, 12, conflict.getResolvedAt());
            pstmt.setString(13, conflict.getResolvedBy());
            setTimestamp(pstmt, 14, conflict.getCreatedAt());
            pstmt.setString(15, conflict.getMetadata());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    conflict.setId(rs.getLong(1));
                }
            }
            logger.debug("Conflict saved: {}", conflict);

        } catch (SQLException e) {
            logger.error("Error saving conflict", e);
            throw new RuntimeException("Failed to save conflict", e);
        }
    }

    /**
     * Find all pending (unresolved) conflicts
     */
    public List<SyncConflict> findPendingConflicts() {
        String sql = "SELECT * FROM sync_conflict WHERE resolution = 'PENDING' ORDER BY created_at DESC";
        return findBySql(sql);
    }

    /**
     * Find pending conflicts by entity type
     */
    public List<SyncConflict> findPendingByEntityType(String entityType) {
        String sql = "SELECT * FROM sync_conflict WHERE resolution = 'PENDING' AND entity_type = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entityType);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<SyncConflict> conflicts = new ArrayList<>();
                while (rs.next()) {
                    conflicts.add(mapResultSet(rs));
                }
                return conflicts;
            }

        } catch (SQLException e) {
            logger.error("Error finding pending conflicts by type", e);
            throw new RuntimeException("Failed to find pending conflicts", e);
        }
    }

    /**
     * Find conflict by ID
     */
    public Optional<SyncConflict> findById(Long id) {
        String sql = "SELECT * FROM sync_conflict WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding conflict by ID", e);
            throw new RuntimeException("Failed to find conflict", e);
        }
    }

    /**
     * Find existing conflict for an entity
     */
    public Optional<SyncConflict> findByEntity(String entityType, String entityId) {
        String sql = "SELECT * FROM sync_conflict WHERE entity_type = ? AND entity_id = ? AND resolution = 'PENDING'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entityType);
            pstmt.setString(2, entityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding conflict by entity", e);
            throw new RuntimeException("Failed to find conflict", e);
        }
    }

    /**
     * Update conflict resolution
     */
    public void updateResolution(Long id, Resolution resolution, String resolvedBy) {
        String sql = "UPDATE sync_conflict SET resolution = ?, resolved_at = ?, resolved_by = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resolution.name());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, resolvedBy);
            pstmt.setLong(4, id);

            pstmt.executeUpdate();
            logger.debug("Conflict {} resolved as {} by {}", id, resolution, resolvedBy);

        } catch (SQLException e) {
            logger.error("Error updating conflict resolution", e);
            throw new RuntimeException("Failed to update conflict", e);
        }
    }

    /**
     * Count pending conflicts
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM sync_conflict WHERE resolution = 'PENDING'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error counting pending conflicts", e);
            throw new RuntimeException("Failed to count conflicts", e);
        }
    }

    /**
     * Delete resolved conflicts older than specified days
     */
    public int deleteOldResolved(int daysOld) {
        String sql = "DELETE FROM sync_conflict WHERE resolution != 'PENDING' AND resolved_at < ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusDays(daysOld)));

            int deleted = pstmt.executeUpdate();
            logger.info("Deleted {} old resolved conflicts", deleted);
            return deleted;

        } catch (SQLException e) {
            logger.error("Error deleting old conflicts", e);
            throw new RuntimeException("Failed to delete old conflicts", e);
        }
    }

    /**
     * Find recent conflicts (for history display)
     */
    public List<SyncConflict> findRecent(int limit) {
        String sql = "SELECT * FROM sync_conflict ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<SyncConflict> conflicts = new ArrayList<>();
                while (rs.next()) {
                    conflicts.add(mapResultSet(rs));
                }
                return conflicts;
            }

        } catch (SQLException e) {
            logger.error("Error finding recent conflicts", e);
            throw new RuntimeException("Failed to find recent conflicts", e);
        }
    }

    private List<SyncConflict> findBySql(String sql) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<SyncConflict> conflicts = new ArrayList<>();
            while (rs.next()) {
                conflicts.add(mapResultSet(rs));
            }
            return conflicts;

        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    private SyncConflict mapResultSet(ResultSet rs) throws SQLException {
        SyncConflict conflict = new SyncConflict();
        conflict.setId(rs.getLong("id"));
        conflict.setEntityType(rs.getString("entity_type"));
        conflict.setEntityId(rs.getString("entity_id"));
        conflict.setFieldName(rs.getString("field_name"));
        conflict.setLocalValue(rs.getString("local_value"));
        conflict.setServerValue(rs.getString("server_value"));
        conflict.setLocalVersion(getIntOrNull(rs, "local_version"));
        conflict.setServerVersion(getIntOrNull(rs, "server_version"));
        conflict.setLocalTimestamp(toLocalDateTime(rs.getTimestamp("local_timestamp")));
        conflict.setServerTimestamp(toLocalDateTime(rs.getTimestamp("server_timestamp")));
        conflict.setConflictType(ConflictType.valueOf(rs.getString("conflict_type")));
        String resolution = rs.getString("resolution");
        conflict.setResolution(resolution != null ? Resolution.valueOf(resolution) : Resolution.PENDING);
        conflict.setResolvedAt(toLocalDateTime(rs.getTimestamp("resolved_at")));
        conflict.setResolvedBy(rs.getString("resolved_by"));
        conflict.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        conflict.setMetadata(rs.getString("metadata"));
        return conflict;
    }

    private void setTimestamp(PreparedStatement pstmt, int index, LocalDateTime dateTime) throws SQLException {
        if (dateTime != null) {
            pstmt.setTimestamp(index, Timestamp.valueOf(dateTime));
        } else {
            pstmt.setNull(index, Types.TIMESTAMP);
        }
    }

    private void setIntOrNull(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (value != null) {
            pstmt.setInt(index, value);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
