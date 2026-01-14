package com.heronix.edu.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heronix.edu.server.dto.GameMetadata;
import com.heronix.edu.server.entity.GameEntity;
import com.heronix.edu.server.repository.GameRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that scans the Heronix-games directory for game.json files
 * and automatically registers/updates games in the database.
 */
@Service
public class GameScannerService {

    private static final Logger logger = LoggerFactory.getLogger(GameScannerService.class);
    private static final String GAME_METADATA_FILE = "game.json";

    @Value("${heronix.games.directory:./Heronix-games}")
    private String gamesDirectory;

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

    // Cache of loaded game metadata
    private final Map<String, GameMetadata> gameMetadataCache = new ConcurrentHashMap<>();

    public GameScannerService(GameRepository gameRepository, ObjectMapper objectMapper) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Scan games directory on startup
     */
    @PostConstruct
    public void scanOnStartup() {
        logger.info("Scanning games directory on startup: {}", gamesDirectory);
        scanGamesDirectory();
    }

    /**
     * Periodically scan for new/updated games (every 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledScan() {
        logger.debug("Scheduled scan of games directory");
        scanGamesDirectory();
    }

    /**
     * Manually trigger a scan (can be called from API)
     */
    public List<GameMetadata> scanGamesDirectory() {
        List<GameMetadata> discoveredGames = new ArrayList<>();
        Path gamesPath = Paths.get(gamesDirectory);

        if (!Files.exists(gamesPath)) {
            logger.warn("Games directory does not exist: {}", gamesDirectory);
            try {
                Files.createDirectories(gamesPath);
                logger.info("Created games directory: {}", gamesDirectory);
            } catch (IOException e) {
                logger.error("Failed to create games directory", e);
                return discoveredGames;
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(gamesPath)) {
            for (Path gameFolder : stream) {
                if (Files.isDirectory(gameFolder)) {
                    GameMetadata metadata = scanGameFolder(gameFolder);
                    if (metadata != null) {
                        discoveredGames.add(metadata);
                        registerOrUpdateGame(metadata, gameFolder);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error scanning games directory", e);
        }

        logger.info("Scan complete. Found {} games", discoveredGames.size());
        return discoveredGames;
    }

    /**
     * Scan a single game folder for game.json
     */
    private GameMetadata scanGameFolder(Path gameFolder) {
        Path metadataFile = gameFolder.resolve(GAME_METADATA_FILE);

        if (!Files.exists(metadataFile)) {
            logger.debug("No game.json found in: {}", gameFolder.getFileName());
            return null;
        }

        try {
            GameMetadata metadata = objectMapper.readValue(metadataFile.toFile(), GameMetadata.class);

            // Validate required fields
            if (metadata.getGameId() == null || metadata.getJarFile() == null) {
                logger.warn("Invalid game.json in {}: missing gameId or jarFile", gameFolder.getFileName());
                return null;
            }

            // Check if JAR file exists
            Path jarPath = gameFolder.resolve(metadata.getJarFile());
            if (!Files.exists(jarPath)) {
                logger.warn("JAR file not found for game {}: {}", metadata.getGameId(), jarPath);
                return null;
            }

            // Compute file size and checksum
            metadata.setFileSizeBytes(Files.size(jarPath));
            metadata.setChecksum(computeChecksum(jarPath));

            // Set URL paths for icon and screenshots
            if (metadata.getIcon() != null) {
                metadata.setIconUrl("/api/games/" + metadata.getGameId() + "/icon");
            }
            if (metadata.getScreenshots() != null && !metadata.getScreenshots().isEmpty()) {
                List<String> screenshotUrls = new ArrayList<>();
                for (int i = 0; i < metadata.getScreenshots().size(); i++) {
                    screenshotUrls.add("/api/games/" + metadata.getGameId() + "/screenshot/" + i);
                }
                metadata.setScreenshotUrls(screenshotUrls);
            }

            // Cache the metadata
            gameMetadataCache.put(metadata.getGameId(), metadata);

            logger.info("Loaded game metadata: {} v{} ({})",
                    metadata.getName(), metadata.getVersion(), metadata.getSubject());

            return metadata;

        } catch (IOException e) {
            logger.error("Error reading game.json from {}", gameFolder.getFileName(), e);
            return null;
        }
    }

    /**
     * Register a new game or update existing game in the database
     */
    private void registerOrUpdateGame(GameMetadata metadata, Path gameFolder) {
        Optional<GameEntity> existingGame = gameRepository.findById(metadata.getGameId());

        GameEntity entity;
        if (existingGame.isPresent()) {
            entity = existingGame.get();
            // Check if update is needed (version or checksum changed)
            if (entity.getVersion().equals(metadata.getVersion()) &&
                entity.getChecksum().equals(metadata.getChecksum())) {
                logger.debug("Game {} is up to date", metadata.getGameId());
                return;
            }
            logger.info("Updating game: {} to version {}", metadata.getGameId(), metadata.getVersion());
        } else {
            entity = new GameEntity();
            entity.setGameId(metadata.getGameId());
            entity.setUploadedAt(LocalDateTime.now());
            logger.info("Registering new game: {}", metadata.getGameId());
        }

        // Update entity fields
        entity.setName(metadata.getName());
        entity.setDescription(metadata.getDescription());
        entity.setVersion(metadata.getVersion());
        entity.setSubject(metadata.getSubject());
        // Convert List to JSON string for storage
        try {
            entity.setTargetGrades(objectMapper.writeValueAsString(metadata.getTargetGrades()));
        } catch (IOException e) {
            entity.setTargetGrades("[]");
        }
        entity.setJarFileName(gameFolder.getFileName().toString() + "/" + metadata.getJarFile());
        entity.setFileSizeBytes(metadata.getFileSizeBytes());
        entity.setChecksum(metadata.getChecksum());
        entity.setActive(metadata.isActive());

        gameRepository.save(entity);
    }

    /**
     * Compute SHA-256 checksum of a file
     */
    private String computeChecksum(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Error computing checksum", e);
            return "error";
        }
    }

    /**
     * Get cached metadata for a game
     */
    public Optional<GameMetadata> getGameMetadata(String gameId) {
        return Optional.ofNullable(gameMetadataCache.get(gameId));
    }

    /**
     * Get all cached game metadata
     */
    public Collection<GameMetadata> getAllGameMetadata() {
        return gameMetadataCache.values();
    }

    /**
     * Get the path to a game's folder
     */
    public Optional<Path> getGameFolder(String gameId) {
        GameMetadata metadata = gameMetadataCache.get(gameId);
        if (metadata == null) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(gamesDirectory, gameId));
    }

    /**
     * Get the path to a game's icon file
     */
    public Optional<Path> getGameIconPath(String gameId) {
        GameMetadata metadata = gameMetadataCache.get(gameId);
        if (metadata == null || metadata.getIcon() == null) {
            return Optional.empty();
        }
        Path iconPath = Paths.get(gamesDirectory, gameId, metadata.getIcon());
        return Files.exists(iconPath) ? Optional.of(iconPath) : Optional.empty();
    }

    /**
     * Get the path to a game's screenshot
     */
    public Optional<Path> getGameScreenshotPath(String gameId, int index) {
        GameMetadata metadata = gameMetadataCache.get(gameId);
        if (metadata == null || metadata.getScreenshots() == null ||
            index < 0 || index >= metadata.getScreenshots().size()) {
            return Optional.empty();
        }
        Path screenshotPath = Paths.get(gamesDirectory, gameId, metadata.getScreenshots().get(index));
        return Files.exists(screenshotPath) ? Optional.of(screenshotPath) : Optional.empty();
    }
}
