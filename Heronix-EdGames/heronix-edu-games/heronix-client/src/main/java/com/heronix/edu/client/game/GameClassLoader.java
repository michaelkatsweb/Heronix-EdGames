package com.heronix.edu.client.game;

import com.heronix.edu.common.game.EducationalGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Custom ClassLoader for loading game JAR files dynamically
 * Uses ServiceLoader to discover EducationalGame implementations
 */
public class GameClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(GameClassLoader.class);

    /**
     * Create a ClassLoader for a game JAR file
     */
    public GameClassLoader(Path jarPath) throws Exception {
        super(new URL[]{jarPath.toUri().toURL()},
              GameClassLoader.class.getClassLoader());

        logger.debug("Created GameClassLoader for: {}", jarPath);
    }

    /**
     * Load a game instance from the JAR by gameId
     * Uses ServiceLoader to find the matching EducationalGame implementation
     *
     * @param gameId The unique identifier of the game to load
     * @return The matching EducationalGame instance
     */
    public EducationalGame loadGameInstance(String gameId) {
        logger.debug("Loading game instance via ServiceLoader for gameId: {}", gameId);

        ServiceLoader<EducationalGame> loader =
            ServiceLoader.load(EducationalGame.class, this);

        // Find the game with matching gameId
        for (EducationalGame game : loader) {
            logger.debug("Found game in JAR: {} (id: {})", game.getName(), game.getGameId());
            if (game.getGameId().equals(gameId)) {
                logger.info("Loaded game: {} (matched gameId: {})", game.getName(), gameId);
                return game;
            }
        }

        // If not found by exact gameId, throw error with available games
        StringBuilder availableGames = new StringBuilder();
        for (EducationalGame game : loader) {
            availableGames.append(game.getGameId()).append(", ");
        }

        throw new IllegalStateException(
            "No EducationalGame found with gameId '" + gameId + "'. " +
            "Available games in JAR: " + availableGames);
    }

    /**
     * Load a game instance from the JAR (loads first found - for backwards compatibility)
     * Uses ServiceLoader to find EducationalGame implementations
     * @deprecated Use loadGameInstance(String gameId) instead
     */
    @Deprecated
    public EducationalGame loadGameInstance() {
        logger.debug("Loading first game instance via ServiceLoader (deprecated method)");

        ServiceLoader<EducationalGame> loader =
            ServiceLoader.load(EducationalGame.class, this);

        EducationalGame game = loader.findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No EducationalGame implementation found in JAR. " +
                "Ensure the JAR has a META-INF/services file."));

        logger.info("Loaded game: {}", game.getName());
        return game;
    }

    /**
     * Close the ClassLoader and release resources
     */
    @Override
    public void close() {
        try {
            super.close();
            logger.debug("GameClassLoader closed");
        } catch (Exception e) {
            logger.error("Error closing GameClassLoader", e);
        }
    }
}
