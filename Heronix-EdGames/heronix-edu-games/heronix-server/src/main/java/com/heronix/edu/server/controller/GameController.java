package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.GameInfoDto;
import com.heronix.edu.server.dto.GameMetadata;
import com.heronix.edu.server.service.GameScannerService;
import com.heronix.edu.server.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for game download and management
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;
    private final GameScannerService gameScannerService;

    public GameController(GameService gameService, GameScannerService gameScannerService) {
        this.gameService = gameService;
        this.gameScannerService = gameScannerService;
    }

    /**
     * List all available games
     * GET /api/games/list
     */
    @GetMapping("/list")
    public ResponseEntity<List<GameInfoDto>> listGames(Authentication auth) {
        logger.info("Listing all games for user: {}", (auth != null ? auth.getName() : "anonymous"));
        List<GameInfoDto> games = gameService.listAllGames();
        logger.debug("Found {} active games", games.size());
        return ResponseEntity.ok(games);
    }

    /**
     * Get specific game information
     * GET /api/games/{gameId}/info
     */
    @GetMapping("/{gameId}/info")
    public ResponseEntity<GameInfoDto> getGameInfo(
            @PathVariable String gameId,
            Authentication auth) {
        logger.info("Getting game info for gameId: {} (user: {})", gameId, (auth != null ? auth.getName() : "anonymous"));
        GameInfoDto gameInfo = gameService.getGameInfo(gameId);
        return ResponseEntity.ok(gameInfo);
    }

    /**
     * Download game JAR file
     * GET /api/games/{gameId}/download
     */
    @GetMapping("/{gameId}/download")
    public ResponseEntity<Resource> downloadGame(
            @PathVariable String gameId,
            Authentication auth) {

        logger.info("Game download requested - gameId: {}, user: {}", gameId, (auth != null ? auth.getName() : "anonymous"));

        try {
            Resource jarResource = gameService.getGameJar(gameId);

            // Get game info for filename
            GameInfoDto gameInfo = gameService.getGameInfo(gameId);
            String filename = gameId + ".jar";

            logger.info("Serving game: {} ({})", gameInfo.getName(), filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(jarResource.contentLength())
                    .body(jarResource);

        } catch (Exception e) {
            logger.error("Error serving game download for gameId: {}", gameId, e);
            throw new RuntimeException("Failed to download game", e);
        }
    }

    /**
     * Get games by subject
     * GET /api/games/subject/{subject}
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<List<GameInfoDto>> getGamesBySubject(
            @PathVariable String subject,
            Authentication auth) {
        logger.info("Getting games for subject: {} (user: {})", subject, (auth != null ? auth.getName() : "anonymous"));
        List<GameInfoDto> games = gameService.getGamesBySubject(subject);
        logger.debug("Found {} games for subject: {}", games.size(), subject);
        return ResponseEntity.ok(games);
    }

    /**
     * Get detailed game metadata from game.json
     * GET /api/games/{gameId}/metadata
     */
    @GetMapping("/{gameId}/metadata")
    public ResponseEntity<GameMetadata> getGameMetadata(@PathVariable String gameId) {
        logger.debug("Getting metadata for game: {}", gameId);
        Optional<GameMetadata> metadata = gameScannerService.getGameMetadata(gameId);
        return metadata.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all game metadata (for store display)
     * GET /api/games/metadata/all
     */
    @GetMapping("/metadata/all")
    public ResponseEntity<Collection<GameMetadata>> getAllGameMetadata() {
        logger.debug("Getting all game metadata");
        Collection<GameMetadata> allMetadata = gameScannerService.getAllGameMetadata();
        return ResponseEntity.ok(allMetadata);
    }

    /**
     * Get game icon image
     * GET /api/games/{gameId}/icon
     */
    @GetMapping("/{gameId}/icon")
    public ResponseEntity<Resource> getGameIcon(@PathVariable String gameId) {
        logger.debug("Getting icon for game: {}", gameId);
        Optional<Path> iconPath = gameScannerService.getGameIconPath(gameId);

        if (iconPath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(iconPath.get().toUri());
            String contentType = determineImageContentType(iconPath.get().toString());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache for 24 hours
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error serving icon for game: {}", gameId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get game screenshot
     * GET /api/games/{gameId}/screenshot/{index}
     */
    @GetMapping("/{gameId}/screenshot/{index}")
    public ResponseEntity<Resource> getGameScreenshot(
            @PathVariable String gameId,
            @PathVariable int index) {
        logger.debug("Getting screenshot {} for game: {}", index, gameId);
        Optional<Path> screenshotPath = gameScannerService.getGameScreenshotPath(gameId, index);

        if (screenshotPath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(screenshotPath.get().toUri());
            String contentType = determineImageContentType(screenshotPath.get().toString());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error serving screenshot for game: {}", gameId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Trigger a manual scan of the games directory
     * POST /api/games/scan
     */
    @PostMapping("/scan")
    public ResponseEntity<List<GameMetadata>> scanGames(Authentication auth) {
        logger.info("Manual game scan triggered by: {}", (auth != null ? auth.getName() : "anonymous"));
        List<GameMetadata> games = gameScannerService.scanGamesDirectory();
        return ResponseEntity.ok(games);
    }

    /**
     * Determine content type based on file extension
     */
    private String determineImageContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
