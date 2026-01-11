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
     * Load a game instance from the JAR
     * Uses ServiceLoader to find EducationalGame implementations
     */
    public EducationalGame loadGameInstance() {
        logger.debug("Loading game instance via ServiceLoader");

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
