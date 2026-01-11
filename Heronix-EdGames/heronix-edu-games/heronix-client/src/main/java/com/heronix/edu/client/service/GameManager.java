package com.heronix.edu.client.service;

import com.heronix.edu.client.config.AppConfig;
import com.heronix.edu.client.db.entity.InstalledGame;
import com.heronix.edu.client.db.repository.InstalledGameRepository;
import com.heronix.edu.client.game.GameClassLoader;
import com.heronix.edu.common.game.EducationalGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing installed games
 * Handles game installation, loading, and metadata
 */
public class GameManager {
    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

    private final InstalledGameRepository gameRepository;
    private final Path gamesDirectory;

    public GameManager(InstalledGameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.gamesDirectory = Paths.get(AppConfig.getGamesDirectory());

        // Create games directory if it doesn't exist
        try {
            Files.createDirectories(gamesDirectory);
            logger.info("Games directory: {}", gamesDirectory);
        } catch (Exception e) {
            logger.error("Failed to create games directory", e);
        }
    }

    /**
     * Get all installed games
     */
    public List<InstalledGame> getInstalledGames() {
        return gameRepository.findAll();
    }

    /**
     * Get a specific installed game
     */
    public Optional<InstalledGame> getInstalledGame(String gameId) {
        return gameRepository.findById(gameId);
    }

    /**
     * Load a game instance from an installed game
     */
    public EducationalGame loadGame(String gameId) throws Exception {
        logger.info("Loading game: {}", gameId);

        InstalledGame game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        Path jarPath = Paths.get(game.getJarPath());

        if (!Files.exists(jarPath)) {
            throw new IllegalStateException("Game JAR not found: " + jarPath);
        }

        // Verify checksum if available
        if (game.getJarChecksum() != null) {
            String actualChecksum = calculateChecksum(jarPath);
            if (!actualChecksum.equals(game.getJarChecksum())) {
                logger.warn("Checksum mismatch for game {}: expected {} but got {}",
                    gameId, game.getJarChecksum(), actualChecksum);
                throw new IllegalStateException("Game JAR checksum mismatch - file may be corrupted");
            }
        }

        // Create ClassLoader and load game
        GameClassLoader classLoader = new GameClassLoader(jarPath);
        EducationalGame gameInstance = classLoader.loadGameInstance();

        logger.info("Game loaded successfully: {}", gameInstance.getName());
        return gameInstance;
    }

    /**
     * Install a game from a local JAR file
     * This is used for development/testing or manual installation
     */
    public void installGame(Path jarPath, String gameId, String gameName) throws Exception {
        logger.info("Installing game {} from {}", gameId, jarPath);

        if (!Files.exists(jarPath)) {
            throw new IllegalArgumentException("JAR file not found: " + jarPath);
        }

        // Calculate checksum
        String checksum = calculateChecksum(jarPath);

        // Copy JAR to games directory
        Path destPath = gamesDirectory.resolve(gameId + ".jar");
        Files.copy(jarPath, destPath);

        long fileSize = Files.size(destPath);

        // Load game temporarily to extract metadata
        GameClassLoader classLoader = new GameClassLoader(destPath);
        try {
            EducationalGame game = classLoader.loadGameInstance();

            // Save to database
            InstalledGame installedGame = new InstalledGame();
            installedGame.setGameId(gameId);
            installedGame.setGameName(gameName != null ? gameName : game.getName());
            installedGame.setDescription(game.getDescription());
            installedGame.setSubject(game.getSubject().toString());
            installedGame.setJarPath(destPath.toString());
            installedGame.setJarChecksum(checksum);
            installedGame.setInstalledAt(LocalDateTime.now());
            installedGame.setFileSizeBytes(fileSize);

            gameRepository.save(installedGame);

            logger.info("Game installed successfully: {}", gameId);

        } finally {
            classLoader.close();
        }
    }

    /**
     * Uninstall a game
     */
    public void uninstallGame(String gameId) throws Exception {
        logger.info("Uninstalling game: {}", gameId);

        InstalledGame game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        // Delete JAR file
        Path jarPath = Paths.get(game.getJarPath());
        if (Files.exists(jarPath)) {
            Files.delete(jarPath);
            logger.debug("Deleted JAR file: {}", jarPath);
        }

        // Remove from database
        gameRepository.delete(gameId);

        logger.info("Game uninstalled successfully: {}", gameId);
    }

    /**
     * Update last played timestamp
     */
    public void updateLastPlayed(String gameId) {
        gameRepository.updateLastPlayed(gameId);
    }

    /**
     * Calculate SHA-256 checksum of a file
     */
    private String calculateChecksum(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = digest.digest(fileBytes);
        return HexFormat.of().formatHex(hashBytes);
    }

    /**
     * Get games directory path
     */
    public Path getGamesDirectory() {
        return gamesDirectory;
    }
}
