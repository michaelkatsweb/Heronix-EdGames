package com.heronix.edu.games.crossword.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Owl mascot "Heronix" with emotional reactions.
 * Provides encouraging feedback during gameplay.
 */
public class OwlMascot extends VBox {

    /**
     * Owl emotional states.
     */
    public enum Reaction {
        NEUTRAL("🦉", "Ready to help!"),
        HAPPY("🦉✨", "Great job!"),
        VERY_HAPPY("🦉🎉", "Amazing!"),
        THINKING("🦉🤔", "Hmm, try again..."),
        ENCOURAGING("🦉💪", "You can do it!"),
        CELEBRATING("🦉🎊", "You did it!"),
        HINT("🦉💡", "Here's a hint!"),
        WAITING("🦉⏳", "Take your time...");

        private final String emoji;
        private final String defaultMessage;

        Reaction(String emoji, String defaultMessage) {
            this.emoji = emoji;
            this.defaultMessage = defaultMessage;
        }

        public String getEmoji() {
            return emoji;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final Label emojiLabel;
    private final Label messageLabel;
    private final HBox container;

    private Reaction currentReaction = Reaction.NEUTRAL;
    private Timeline pulseAnimation;
    private Timeline bounceAnimation;

    public OwlMascot() {
        setAlignment(Pos.CENTER);
        setSpacing(5);

        // Emoji display
        emojiLabel = new Label(Reaction.NEUTRAL.getEmoji());
        emojiLabel.setFont(Font.font("Segoe UI Emoji", 48));

        // Message bubble
        messageLabel = new Label(Reaction.NEUTRAL.getDefaultMessage());
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.rgb(60, 60, 60));
        messageLabel.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; " +
                             "-fx-padding: 8 15 8 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(emojiLabel, messageLabel);

        getChildren().add(container);

        setupAnimations();
    }

    private void setupAnimations() {
        // Pulse animation for emphasis
        pulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(emojiLabel.scaleXProperty(), 1.0),
                new KeyValue(emojiLabel.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(200),
                new KeyValue(emojiLabel.scaleXProperty(), 1.2),
                new KeyValue(emojiLabel.scaleYProperty(), 1.2)),
            new KeyFrame(Duration.millis(400),
                new KeyValue(emojiLabel.scaleXProperty(), 1.0),
                new KeyValue(emojiLabel.scaleYProperty(), 1.0))
        );

        // Bounce animation for celebrations
        bounceAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(emojiLabel.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(150),
                new KeyValue(emojiLabel.translateYProperty(), -15)),
            new KeyFrame(Duration.millis(300),
                new KeyValue(emojiLabel.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(450),
                new KeyValue(emojiLabel.translateYProperty(), -10)),
            new KeyFrame(Duration.millis(600),
                new KeyValue(emojiLabel.translateYProperty(), 0))
        );
    }

    /**
     * Show a reaction with default message.
     */
    public void showReaction(Reaction reaction) {
        showReaction(reaction, reaction.getDefaultMessage());
    }

    /**
     * Show a reaction with custom message.
     */
    public void showReaction(Reaction reaction, String message) {
        currentReaction = reaction;
        emojiLabel.setText(reaction.getEmoji());
        messageLabel.setText(message);

        // Play appropriate animation
        switch (reaction) {
            case HAPPY:
            case VERY_HAPPY:
                pulseAnimation.playFromStart();
                break;
            case CELEBRATING:
                bounceAnimation.setCycleCount(2);
                bounceAnimation.playFromStart();
                break;
            case HINT:
                pulseAnimation.playFromStart();
                break;
            default:
                // No animation
                break;
        }
    }

    /**
     * Show reaction for correct answer.
     */
    public void onCorrectLetter() {
        showReaction(Reaction.HAPPY, "Nice!");
    }

    /**
     * Show reaction for incorrect answer.
     */
    public void onIncorrectLetter() {
        showReaction(Reaction.THINKING, "Not quite...");
    }

    /**
     * Show reaction when a word is completed.
     */
    public void onWordComplete(String word) {
        showReaction(Reaction.VERY_HAPPY, "'" + word + "' complete!");
    }

    /**
     * Show reaction when puzzle is solved.
     */
    public void onPuzzleComplete() {
        showReaction(Reaction.CELEBRATING, "Puzzle complete! Amazing work!");
    }

    /**
     * Show reaction when hint is used.
     */
    public void onHintUsed() {
        showReaction(Reaction.HINT, "Here's some help!");
    }

    /**
     * Show encouraging message.
     */
    public void encourage() {
        String[] messages = {
            "Keep going!",
            "You're doing great!",
            "Almost there!",
            "Don't give up!",
            "You've got this!"
        };
        int index = (int) (Math.random() * messages.length);
        showReaction(Reaction.ENCOURAGING, messages[index]);
    }

    /**
     * Show neutral/waiting state.
     */
    public void reset() {
        showReaction(Reaction.NEUTRAL);
    }

    /**
     * Get current reaction.
     */
    public Reaction getCurrentReaction() {
        return currentReaction;
    }

    /**
     * Create a compact version for smaller displays.
     */
    public static OwlMascot createCompact() {
        OwlMascot mascot = new OwlMascot();
        mascot.emojiLabel.setFont(Font.font("Segoe UI Emoji", 32));
        mascot.messageLabel.setFont(Font.font("Arial", 12));
        return mascot;
    }
}
