package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.GameInfoDto;
import com.heronix.edu.server.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for game download and management
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
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
}
