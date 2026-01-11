package com.heronix.edu.client.db.repository;

import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.entity.InstalledGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for installed game metadata
 */
public class InstalledGameRepository {
    private static final Logger logger = LoggerFactory.getLogger(InstalledGameRepository.class);

    /**
     * Save a new installed game
     */
    public void save(InstalledGame game) {
        String sql = "INSERT INTO installed_game (game_id, game_name, description, version, subject, " +
                     "target_grades, jar_path, jar_checksum, installed_at, last_played_at, file_size_bytes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, game.getGameId());
            pstmt.setString(2, game.getGameName());
            pstmt.setString(3, game.getDescription());
            pstmt.setString(4, game.getVersion());
            pstmt.setString(5, game.getSubject());
            pstmt.setString(6, game.getTargetGrades());
            pstmt.setString(7, game.getJarPath());
            pstmt.setString(8, game.getJarChecksum());
            pstmt.setTimestamp(9, toTimestamp(game.getInstalledAt()));
            pstmt.setTimestamp(10, toTimestamp(game.getLastPlayedAt()));
            setLongOrNull(pstmt, 11, game.getFileSizeBytes());

            pstmt.executeUpdate();
            logger.info("Installed game saved: {}", game.getGameName());

        } catch (SQLException e) {
            logger.error("Error saving installed game", e);
            throw new RuntimeException("Failed to save installed game", e);
        }
    }

    /**
     * Find all installed games
     */
    public List<InstalledGame> findAll() {
        String sql = "SELECT * FROM installed_game ORDER BY game_name";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<InstalledGame> games = new ArrayList<>();
            while (rs.next()) {
                games.add(mapResultSetToGame(rs));
            }
            return games;

        } catch (SQLException e) {
            logger.error("Error finding all games", e);
            throw new RuntimeException("Failed to find games", e);
        }
    }

    /**
     * Find game by ID
     */
    public Optional<InstalledGame> findById(String gameId) {
        String sql = "SELECT * FROM installed_game WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gameId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGame(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding game by ID", e);
            throw new RuntimeException("Failed to find game", e);
        }
    }

    /**
     * Update last played timestamp
     */
    public void updateLastPlayed(String gameId) {
        String sql = "UPDATE installed_game SET last_played_at = ? WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, gameId);

            pstmt.executeUpdate();
            logger.debug("Updated last played for game: {}", gameId);

        } catch (SQLException e) {
            logger.error("Error updating last played", e);
            throw new RuntimeException("Failed to update last played", e);
        }
    }

    /**
     * Delete a game
     */
    public void delete(String gameId) {
        String sql = "DELETE FROM installed_game WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gameId);
            pstmt.executeUpdate();
            logger.info("Deleted game: {}", gameId);

        } catch (SQLException e) {
            logger.error("Error deleting game", e);
            throw new RuntimeException("Failed to delete game", e);
        }
    }

    /**
     * Map ResultSet to InstalledGame entity
     */
    private InstalledGame mapResultSetToGame(ResultSet rs) throws SQLException {
        InstalledGame game = new InstalledGame();
        game.setGameId(rs.getString("game_id"));
        game.setGameName(rs.getString("game_name"));
        game.setDescription(rs.getString("description"));
        game.setVersion(rs.getString("version"));
        game.setSubject(rs.getString("subject"));
        game.setTargetGrades(rs.getString("target_grades"));
        game.setJarPath(rs.getString("jar_path"));
        game.setJarChecksum(rs.getString("jar_checksum"));
        game.setInstalledAt(toLocalDateTime(rs.getTimestamp("installed_at")));
        game.setLastPlayedAt(toLocalDateTime(rs.getTimestamp("last_played_at")));
        game.setFileSizeBytes(getLongOrNull(rs, "file_size_bytes"));
        return game;
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private void setLongOrNull(PreparedStatement pstmt, int index, Long value) throws SQLException {
        if (value != null) {
            pstmt.setLong(index, value);
        } else {
            pstmt.setNull(index, Types.BIGINT);
        }
    }

    private Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
}
