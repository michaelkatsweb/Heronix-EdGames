package com.heronix.edu.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heronix.edu.server.dto.GameInfoDto;
import com.heronix.edu.server.entity.GameEntity;
import com.heronix.edu.server.exception.ResourceNotFoundException;
import com.heronix.edu.server.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing educational games
 */
@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;
    private final Path gamesDirectory;

    public GameService(
            GameRepository gameRepository,
            ObjectMapper objectMapper,
            @Value("${heronix.games.directory:./games}") String gamesDirectoryPath) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
        this.gamesDirectory = Paths.get(gamesDirectoryPath);

        // Ensure games directory exists
        try {
            Files.createDirectories(this.gamesDirectory);
            logger.info("Games directory initialized at: {}", this.gamesDirectory.toAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to create games directory", e);
        }
    }

    /**
     * List all active games
     */
    public List<GameInfoDto> listAllGames() {
        logger.debug("Fetching all active games");
        List<GameEntity> games = gameRepository.findByActiveTrue();
        return games.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get game information by ID
     */
    public GameInfoDto getGameInfo(String gameId) {
        logger.debug("Fetching game info for gameId: {}", gameId);
        GameEntity game = gameRepository.findByGameIdAndActiveTrue(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
        return toDto(game);
    }

    /**
     * Get game JAR file as a resource
     */
    public Resource getGameJar(String gameId) {
        logger.debug("Fetching game JAR for gameId: {}", gameId);

        GameEntity game = gameRepository.findByGameIdAndActiveTrue(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));

        Path jarPath = gamesDirectory.resolve(game.getJarFileName());

        if (!Files.exists(jarPath)) {
            logger.error("Game JAR file not found: {}", jarPath.toAbsolutePath());
            throw new ResourceNotFoundException("Game JAR file not found for: " + gameId);
        }

        logger.info("Serving game JAR: {} ({})", game.getName(), jarPath.getFileName());
        return new FileSystemResource(jarPath);
    }

    /**
     * Get games by subject
     */
    public List<GameInfoDto> getGamesBySubject(String subject) {
        logger.debug("Fetching games for subject: {}", subject);
        List<GameEntity> games = gameRepository.findBySubjectAndActiveTrue(subject);
        return games.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert GameEntity to DTO
     */
    private GameInfoDto toDto(GameEntity entity) {
        GameInfoDto dto = new GameInfoDto();
        dto.setGameId(entity.getGameId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        dto.setSubject(entity.getSubject());
        dto.setFileSizeBytes(entity.getFileSizeBytes());
        dto.setChecksum(entity.getChecksum());
        dto.setUploadedAt(entity.getUploadedAt());

        // Parse target grades from JSON string
        if (entity.getTargetGrades() != null && !entity.getTargetGrades().isEmpty()) {
            try {
                List<String> grades = objectMapper.readValue(
                        entity.getTargetGrades(),
                        new TypeReference<List<String>>() {}
                );
                dto.setTargetGrades(grades);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse target grades for game: {}", entity.getGameId(), e);
                dto.setTargetGrades(new ArrayList<>());
            }
        } else {
            dto.setTargetGrades(new ArrayList<>());
        }

        return dto;
    }
}
