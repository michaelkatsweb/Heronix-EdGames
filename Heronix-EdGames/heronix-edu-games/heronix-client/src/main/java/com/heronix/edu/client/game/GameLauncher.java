package com.heronix.edu.client.game;

import com.heronix.edu.common.game.EducationalGame;
import com.heronix.edu.common.game.GameContext;
import com.heronix.edu.common.game.GameResult;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Launches and manages game execution
 * Games create their own UI windows, so this class primarily manages lifecycle
 */
public class GameLauncher {
    private static final Logger logger = LoggerFactory.getLogger(GameLauncher.class);

    private final EducationalGame game;
    private final GameContext context;
    private Consumer<GameResult> onComplete;

    /**
     * Create a GameLauncher
     */
    public GameLauncher(EducationalGame game, GameContext context) {
        this.game = game;
        this.context = context;
    }

    /**
     * Launch the game
     * The game creates its own UI window
     */
    public void launch(Consumer<GameResult> onComplete) {
        this.onComplete = onComplete;

        logger.info("Launching game: {}", game.getName());

        try {
            // Initialize game with context
            game.initialize(context);

            // Start game (game handles its own UI)
            game.start();

            logger.info("Game launched successfully: {}", game.getName());

        } catch (Exception e) {
            logger.error("Error launching game", e);
            throw new RuntimeException("Failed to launch game: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the game
     * Call this when game is complete and notify completion handler
     */
    public void stop() {
        logger.info("Stopping game: {}", game.getName());

        try {
            game.stop();

            // Get final result
            GameResult result = game.getResult();

            logger.info("Game completed: {}", game.getName());
            logger.info("Score: {}/{}", result.getScore(), result.getMaxScore());

            // Notify completion handler
            if (onComplete != null) {
                Platform.runLater(() -> onComplete.accept(result));
            }

        } catch (Exception e) {
            logger.error("Error stopping game", e);
        }
    }

    /**
     * Get the game instance
     */
    public EducationalGame getGame() {
        return game;
    }
}
