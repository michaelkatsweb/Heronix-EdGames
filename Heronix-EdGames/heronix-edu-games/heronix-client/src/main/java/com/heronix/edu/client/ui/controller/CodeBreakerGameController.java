package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.multiplayer.StompGameClient;
import com.heronix.edu.client.multiplayer.dto.*;
import com.heronix.edu.client.service.ScoreService;
import com.heronix.edu.common.game.GameResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Code Breaker main game screen.
 * Handles real-time game state, questions, hacking, and leaderboard.
 */
public class CodeBreakerGameController {
    private static final Logger logger = LoggerFactory.getLogger(CodeBreakerGameController.class);

    // Top bar elements
    @FXML private Label sessionCodeLabel;
    @FXML private Label playerNameLabel;
    @FXML private Label avatarLabel;
    @FXML private Label creditsLabel;
    @FXML private Label correctLabel;
    @FXML private Label wrongLabel;
    @FXML private Label rankLabel;
    @FXML private VBox shieldBox;
    @FXML private Label timeLabel;

    // Main game panels
    @FXML private VBox waitingPane;
    @FXML private VBox questionPane;
    @FXML private VBox rewardPane;
    @FXML private VBox wrongPane;
    @FXML private VBox challengePane;
    @FXML private VBox gameOverPane;

    // Question elements
    @FXML private Label questionLabel;
    @FXML private VBox answersBox;
    @FXML private ProgressBar questionTimer;
    @FXML private Label waitingPlayersLabel;

    // Reward elements
    @FXML private HBox rewardsBox;

    // Wrong answer elements
    @FXML private Label correctAnswerLabel;

    // Challenge elements
    @FXML private Label hackerNameLabel;
    @FXML private Label challengeQuestionLabel;
    @FXML private HBox challengeAnswersBox;
    @FXML private ProgressBar challengeTimer;

    // Game over elements
    @FXML private Label finalRankLabel;
    @FXML private Label finalCreditsLabel;
    @FXML private Label finalCorrectLabel;
    @FXML private Label finalWrongLabel;
    @FXML private Label finalHacksLabel;

    // Hack panel elements
    @FXML private VBox hackTargetsBox;
    @FXML private VBox hackInputBox;
    @FXML private Label hackTargetLabel;
    @FXML private Label hackHintLabel;
    @FXML private HBox hackCodeButtons;
    @FXML private Button hackButton;

    // Leaderboard and activity
    @FXML private VBox leaderboardBox;
    @FXML private VBox activityBox;

    // Game state
    private StompGameClient wsClient;
    private ScoreService scoreService;
    private Stage stage;
    private Runnable onBack;
    private String sessionCode;
    private String playerName;
    private String avatarId;
    private String secretCode;
    private LocalDateTime gameStartTime;

    private PlayerState playerState;
    private String currentQuestionId;
    private String selectedHackTarget;
    private String selectedHackCode;
    private Timeline questionTimeline;
    private Timeline challengeTimeline;
    private Timeline gameTimeline;

    private static final String[] SECRET_CODES = {"ALPHA", "BETA", "GAMMA", "DELTA", "OMEGA"};

    /**
     * Initialize controller with WebSocket client and game info.
     */
    public void initialize(StompGameClient wsClient, String sessionCode, String playerName,
                          String avatarId, String secretCode, ScoreService scoreService,
                          Stage stage, Runnable onBack) {
        this.wsClient = wsClient;
        this.sessionCode = sessionCode;
        this.playerName = playerName;
        this.avatarId = avatarId;
        this.secretCode = secretCode;
        this.scoreService = scoreService;
        this.stage = stage;
        this.onBack = onBack;
        this.gameStartTime = LocalDateTime.now();

        // Initialize player state
        this.playerState = new PlayerState();
        playerState.setPlayerName(playerName);
        playerState.setAvatarId(avatarId);
        playerState.setSecretCode(secretCode);
        playerState.setCredits(0);

        // Set up UI
        sessionCodeLabel.setText("Session: " + sessionCode);
        playerNameLabel.setText(playerName);
        avatarLabel.setText(getAvatarEmoji(avatarId));
        updatePlayerStats();

        // Set up WebSocket message handlers
        setupMessageHandlers();

        // Show waiting state
        showPanel("waiting");

        logger.info("CodeBreakerGameController initialized for session: {}", sessionCode);
    }

    /**
     * Set up WebSocket message handlers for game events.
     */
    private void setupMessageHandlers() {
        // Game started
        wsClient.onMessage("GAME_STARTED", this::handleGameStarted);

        // New question
        wsClient.onMessage("NEW_QUESTION", this::handleNewQuestion);

        // Answer result
        wsClient.onMessage("ANSWER_RESULT", this::handleAnswerResult);

        // Reward options
        wsClient.onMessage("REWARD_OPTIONS", this::handleRewardOptions);

        // Reward selected confirmation
        wsClient.onMessage("REWARD_SELECTED", this::handleRewardSelected);

        // Player update (credits, stats)
        wsClient.onMessage("PLAYER_UPDATE", this::handlePlayerUpdate);

        // Leaderboard update
        wsClient.onMessage("LEADERBOARD_UPDATE", this::handleLeaderboardUpdate);

        // Being hacked
        wsClient.onMessage("BEING_HACKED", this::handleBeingHacked);

        // Hack result
        wsClient.onMessage("HACK_RESULT", this::handleHackResult);

        // Challenge result
        wsClient.onMessage("CHALLENGE_RESULT", this::handleChallengeResult);

        // Game event (activity feed)
        wsClient.onMessage("GAME_EVENT", this::handleGameEvent);

        // Players update (waiting room)
        wsClient.onMessage("PLAYERS_UPDATE", this::handlePlayersUpdate);

        // Game ended
        wsClient.onMessage("GAME_ENDED", this::handleGameEnded);

        // Connection status
        wsClient.setConnectionStatusHandler(connected -> {
            if (!connected) {
                Platform.runLater(() -> {
                    addActivity("Disconnected from server!", "#ff5252");
                });
            }
        });
    }

    /**
     * Handle game started event.
     */
    private void handleGameStarted(Map<String, Object> msg) {
        logger.info("Game started!");
        Platform.runLater(() -> {
            addActivity("Game started!", "#00ff88");
            showPanel("question");
        });
    }

    /**
     * Handle new question event.
     */
    private void handleNewQuestion(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                currentQuestionId = (String) payload.get("questionId");
                String questionText = (String) payload.get("questionText");
                List<?> options = (List<?>) payload.get("options");
                int timeLimit = payload.get("timeLimit") != null ?
                    ((Number) payload.get("timeLimit")).intValue() : 30;

                questionLabel.setText(questionText);
                answersBox.getChildren().clear();

                for (Object option : options) {
                    Button answerBtn = createAnswerButton((String) option);
                    answersBox.getChildren().add(answerBtn);
                }

                // Start question timer
                startQuestionTimer(timeLimit);
                showPanel("question");

            } catch (Exception e) {
                logger.error("Error handling new question", e);
            }
        });
    }

    /**
     * Create an answer button.
     */
    private Button createAnswerButton(String answer) {
        Button btn = new Button(answer);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                    "-fx-font-size: 16px; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnAction(e -> submitAnswer(answer));

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(0,255,136,0.3); -fx-text-fill: white; " +
                    "-fx-font-size: 16px; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                    "-fx-font-size: 16px; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;"));

        return btn;
    }

    /**
     * Submit answer to server.
     */
    private void submitAnswer(String answer) {
        if (currentQuestionId == null) return;

        // Disable all answer buttons
        answersBox.getChildren().forEach(node -> {
            if (node instanceof Button) {
                ((Button) node).setDisable(true);
            }
        });

        // Stop timer
        if (questionTimeline != null) {
            questionTimeline.stop();
        }

        wsClient.submitAnswer(currentQuestionId, answer);
        logger.info("Submitted answer: {}", answer);
    }

    /**
     * Handle answer result.
     */
    private void handleAnswerResult(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                boolean correct = (Boolean) payload.get("correct");
                String correctAnswer = (String) payload.get("correctAnswer");

                if (correct) {
                    // Will receive REWARD_OPTIONS next
                    addActivity("Correct answer!", "#00ff88");
                } else {
                    correctAnswerLabel.setText("The correct answer was: " + correctAnswer);
                    playerState.setWrongAnswers(playerState.getWrongAnswers() + 1);
                    updatePlayerStats();
                    showPanel("wrong");
                    addActivity("Wrong answer", "#ff5252");
                }
            } catch (Exception e) {
                logger.error("Error handling answer result", e);
            }
        });
    }

    /**
     * Handle reward options.
     */
    private void handleRewardOptions(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                List<?> rewards = (List<?>) payload.get("rewards");

                rewardsBox.getChildren().clear();

                for (Object r : rewards) {
                    Map<?, ?> reward = (Map<?, ?>) r;
                    String type = (String) reward.get("type");
                    int value = ((Number) reward.get("value")).intValue();
                    String description = (String) reward.get("description");

                    VBox rewardCard = createRewardCard(type, value, description);
                    rewardsBox.getChildren().add(rewardCard);
                }

                playerState.setCorrectAnswers(playerState.getCorrectAnswers() + 1);
                updatePlayerStats();
                showPanel("reward");

            } catch (Exception e) {
                logger.error("Error handling reward options", e);
            }
        });
    }

    /**
     * Create a reward selection card.
     */
    private VBox createRewardCard(String type, int value, String description) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 20; " +
                     "-fx-background-radius: 10; -fx-cursor: hand;");
        card.setPrefWidth(150);

        String icon = switch (type) {
            case "CREDITS" -> "💰";
            case "SHIELD" -> "🛡";
            case "CODE_HINT" -> "🔍";
            default -> "🎁";
        };

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label valueLabel = new Label("+" + value);
        valueLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #b39ddb; -fx-font-size: 11px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(130);

        card.getChildren().addAll(iconLabel, valueLabel, descLabel);

        card.setOnMouseClicked(e -> selectReward(type));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(0,255,136,0.3); -fx-padding: 20; " +
                     "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: #00ff88; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 20; " +
                     "-fx-background-radius: 10; -fx-cursor: hand;"));

        return card;
    }

    /**
     * Select a reward.
     */
    private void selectReward(String rewardType) {
        wsClient.selectReward(rewardType);
        logger.info("Selected reward: {}", rewardType);
    }

    /**
     * Handle reward selected confirmation.
     */
    private void handleRewardSelected(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                String type = (String) payload.get("type");
                int value = ((Number) payload.get("value")).intValue();

                if ("CREDITS".equals(type)) {
                    playerState.setCredits(playerState.getCredits() + value);
                } else if ("SHIELD".equals(type)) {
                    playerState.setShielded(true);
                    shieldBox.setVisible(true);
                }

                updatePlayerStats();
                addActivity("Collected reward: +" + value + " " + type.toLowerCase(), "#00ff88");

                // Request next question
                wsClient.requestQuestion();

            } catch (Exception e) {
                logger.error("Error handling reward selected", e);
            }
        });
    }

    /**
     * Handle player update (stats from server).
     */
    private void handlePlayerUpdate(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");

                if (payload.get("credits") != null) {
                    playerState.setCredits(((Number) payload.get("credits")).intValue());
                }
                if (payload.get("rank") != null) {
                    playerState.setRank(((Number) payload.get("rank")).intValue());
                }
                if (payload.get("shielded") != null) {
                    playerState.setShielded((Boolean) payload.get("shielded"));
                    shieldBox.setVisible(playerState.isShielded());
                }

                updatePlayerStats();

            } catch (Exception e) {
                logger.error("Error handling player update", e);
            }
        });
    }

    /**
     * Handle leaderboard update.
     */
    private void handleLeaderboardUpdate(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                List<?> players = (List<?>) msg.get("payload");
                updateLeaderboard(players);
                updateHackTargets(players);
            } catch (Exception e) {
                logger.error("Error handling leaderboard update", e);
            }
        });
    }

    /**
     * Update leaderboard display.
     */
    private void updateLeaderboard(List<?> players) {
        leaderboardBox.getChildren().clear();

        int rank = 1;
        for (Object p : players) {
            Map<?, ?> player = (Map<?, ?>) p;
            String name = (String) player.get("studentName");
            int credits = ((Number) player.get("credits")).intValue();
            String playerId = (String) player.get("playerId");
            boolean isMe = playerId.equals(wsClient.getPlayerId());

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(isMe ? "-fx-background-color: rgba(0,255,136,0.2); -fx-padding: 5 10; -fx-background-radius: 5;" :
                               "-fx-padding: 5 10;");

            Label rankLbl = new Label("#" + rank);
            rankLbl.setStyle("-fx-text-fill: #ffd54f; -fx-font-size: 12px; -fx-min-width: 25;");

            Label nameLbl = new Label(name);
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            nameLbl.setMaxWidth(120);

            Label creditLbl = new Label(credits + " 💰");
            creditLbl.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 12px;");

            row.getChildren().addAll(rankLbl, nameLbl, creditLbl);
            leaderboardBox.getChildren().add(row);

            if (isMe) {
                playerState.setRank(rank);
                rankLabel.setText("Rank: #" + rank);
            }

            rank++;
        }
    }

    /**
     * Update hack targets list.
     */
    private void updateHackTargets(List<?> players) {
        hackTargetsBox.getChildren().clear();
        String myPlayerId = wsClient.getPlayerId();

        for (Object p : players) {
            Map<?, ?> player = (Map<?, ?>) p;
            String playerId = (String) player.get("playerId");

            // Skip self
            if (playerId.equals(myPlayerId)) continue;

            String name = (String) player.get("studentName");
            int credits = ((Number) player.get("credits")).intValue();
            boolean shielded = player.get("shielded") != null && (Boolean) player.get("shielded");

            HBox targetRow = new HBox(10);
            targetRow.setAlignment(Pos.CENTER_LEFT);
            targetRow.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");

            Label nameLbl = new Label(name);
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            nameLbl.setMaxWidth(100);

            Label creditLbl = new Label(credits / 2 + " 💰");
            creditLbl.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px;");

            if (shielded) {
                Label shieldLbl = new Label("🛡");
                targetRow.getChildren().addAll(nameLbl, creditLbl, shieldLbl);
            } else {
                targetRow.getChildren().addAll(nameLbl, creditLbl);
            }

            targetRow.setOnMouseClicked(e -> selectHackTarget(playerId, name));
            targetRow.setOnMouseEntered(e -> targetRow.setStyle("-fx-background-color: rgba(255,82,82,0.3); -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;"));
            targetRow.setOnMouseExited(e -> targetRow.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;"));

            hackTargetsBox.getChildren().add(targetRow);
        }
    }

    /**
     * Select a hack target.
     */
    private void selectHackTarget(String playerId, String playerName) {
        selectedHackTarget = playerId;
        hackTargetLabel.setText("Hacking: " + playerName);
        hackHintLabel.setText("");
        selectedHackCode = null;

        // Setup code buttons
        hackCodeButtons.getChildren().clear();
        ToggleGroup codeGroup = new ToggleGroup();

        for (String code : SECRET_CODES) {
            ToggleButton codeBtn = new ToggleButton(code);
            codeBtn.setToggleGroup(codeGroup);
            codeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5 8;");

            codeBtn.selectedProperty().addListener((obs, was, is) -> {
                if (is) {
                    selectedHackCode = code;
                    codeBtn.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5 8;");
                } else {
                    codeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5 8;");
                }
            });

            hackCodeButtons.getChildren().add(codeBtn);
        }

        hackInputBox.setVisible(true);
    }

    /**
     * Handle hack attempt button.
     */
    @FXML
    public void handleHackAttempt() {
        if (selectedHackTarget == null || selectedHackCode == null) return;

        hackButton.setDisable(true);
        wsClient.attemptHack(selectedHackTarget, selectedHackCode);
        logger.info("Attempting hack on {} with code {}", selectedHackTarget, selectedHackCode);
    }

    /**
     * Handle being hacked notification.
     */
    private void handleBeingHacked(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                String hackerName = (String) payload.get("hackerName");
                String question = (String) payload.get("challengeQuestion");
                List<?> options = (List<?>) payload.get("options");
                int timeLimit = payload.get("timeLimit") != null ?
                    ((Number) payload.get("timeLimit")).intValue() : 10;

                hackerNameLabel.setText(hackerName + " is trying to steal your credits!");
                challengeQuestionLabel.setText(question);

                challengeAnswersBox.getChildren().clear();
                for (Object opt : options) {
                    Button answerBtn = new Button((String) opt);
                    answerBtn.setStyle("-fx-background-color: rgba(255,152,0,0.3); -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
                    answerBtn.setOnAction(e -> submitChallengeAnswer((String) opt));
                    challengeAnswersBox.getChildren().add(answerBtn);
                }

                startChallengeTimer(timeLimit);
                showPanel("challenge");
                addActivity("Being hacked by " + hackerName + "!", "#ff9800");

            } catch (Exception e) {
                logger.error("Error handling being hacked", e);
            }
        });
    }

    /**
     * Submit challenge answer.
     */
    private void submitChallengeAnswer(String answer) {
        if (challengeTimeline != null) {
            challengeTimeline.stop();
        }
        wsClient.submitChallengeAnswer(answer);
    }

    /**
     * Handle hack result.
     */
    private void handleHackResult(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                boolean success = (Boolean) payload.get("success");
                String message = (String) payload.get("message");

                if (success) {
                    int stolen = ((Number) payload.get("creditsStolen")).intValue();
                    playerState.setCredits(playerState.getCredits() + stolen);
                    playerState.setSuccessfulHacks(playerState.getSuccessfulHacks() + 1);
                    addActivity("Hacked! Stole " + stolen + " credits!", "#00ff88");
                } else {
                    // Show hint if provided
                    if (payload.get("hint") != null) {
                        hackHintLabel.setText("Hint: " + payload.get("hint"));
                    }
                    addActivity("Hack failed: " + message, "#ff5252");
                }

                hackButton.setDisable(false);
                updatePlayerStats();

            } catch (Exception e) {
                logger.error("Error handling hack result", e);
            }
        });
    }

    /**
     * Handle challenge result (defending against hack).
     */
    private void handleChallengeResult(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                boolean success = (Boolean) payload.get("success");

                if (success) {
                    addActivity("Defended hack successfully!", "#00ff88");
                } else {
                    int lost = ((Number) payload.get("creditsLost")).intValue();
                    playerState.setCredits(Math.max(0, playerState.getCredits() - lost));
                    playerState.setTimesHacked(playerState.getTimesHacked() + 1);
                    addActivity("Failed defense! Lost " + lost + " credits", "#ff5252");
                }

                updatePlayerStats();
                showPanel("question");
                wsClient.requestQuestion();

            } catch (Exception e) {
                logger.error("Error handling challenge result", e);
            }
        });
    }

    /**
     * Handle game event for activity feed.
     */
    private void handleGameEvent(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");
                String eventText = (String) payload.get("text");
                String color = (String) payload.get("color");
                addActivity(eventText, color != null ? color : "#b39ddb");
            } catch (Exception e) {
                logger.error("Error handling game event", e);
            }
        });
    }

    /**
     * Handle players update in waiting room.
     */
    private void handlePlayersUpdate(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                int count = ((Number) msg.get("payload")).intValue();
                waitingPlayersLabel.setText("Players joined: " + count);
            } catch (Exception e) {
                logger.error("Error handling players update", e);
            }
        });
    }

    /**
     * Handle game ended.
     */
    private void handleGameEnded(Map<String, Object> msg) {
        Platform.runLater(() -> {
            try {
                Map<?, ?> payload = (Map<?, ?>) msg.get("payload");

                if (gameTimeline != null) gameTimeline.stop();
                if (questionTimeline != null) questionTimeline.stop();

                finalRankLabel.setText("You finished in #" + playerState.getRank() + " place!");
                finalCreditsLabel.setText("Final Credits: " + playerState.getCredits());
                finalCorrectLabel.setText(String.valueOf(playerState.getCorrectAnswers()));
                finalWrongLabel.setText(String.valueOf(playerState.getWrongAnswers()));
                finalHacksLabel.setText(String.valueOf(playerState.getSuccessfulHacks()));

                // Save game score locally
                saveGameScore();

                showPanel("gameover");
                addActivity("Game Over!", "#ffd54f");

            } catch (Exception e) {
                logger.error("Error handling game ended", e);
            }
        });
    }

    /**
     * Save the game score to local database for sync.
     */
    private void saveGameScore() {
        try {
            if (scoreService == null) {
                logger.warn("ScoreService not available, skipping score save");
                return;
            }

            // Calculate elapsed time
            LocalDateTime endTime = LocalDateTime.now();
            int elapsedSeconds = (int) ChronoUnit.SECONDS.between(gameStartTime, endTime);

            // Create GameResult
            GameResult result = new GameResult("code-breaker-multiplayer");
            result.setScore(playerState.getCredits());
            result.setMaxScore(Math.max(playerState.getCredits(), 1000)); // Use credits as score, max is dynamic
            result.setCorrectAnswers(playerState.getCorrectAnswers());
            result.setIncorrectAnswers(playerState.getWrongAnswers());
            result.setTimeElapsedSeconds(elapsedSeconds);
            result.setCompleted(true);
            result.setStartTime(gameStartTime);
            result.setEndTime(endTime);

            // Add custom metrics for Code Breaker
            result.addCustomMetric("successfulHacks", playerState.getSuccessfulHacks());
            result.addCustomMetric("timesHacked", playerState.getTimesHacked());
            result.addCustomMetric("finalRank", playerState.getRank());
            result.addCustomMetric("sessionCode", sessionCode);
            result.addCustomMetric("avatarId", avatarId);

            // Save to local database (will be synced later)
            scoreService.saveGameScore(result);

            logger.info("Game score saved: credits={}, correct={}, wrong={}, hacks={}, time={}s",
                playerState.getCredits(), playerState.getCorrectAnswers(), playerState.getWrongAnswers(),
                playerState.getSuccessfulHacks(), elapsedSeconds);

            addActivity("Score saved!", "#00ff88");

        } catch (Exception e) {
            logger.error("Failed to save game score", e);
            addActivity("Failed to save score", "#ff5252");
        }
    }

    /**
     * Handle next question button (after wrong answer).
     */
    @FXML
    public void handleNextQuestion() {
        wsClient.requestQuestion();
        showPanel("question");
    }

    /**
     * Handle back to launcher button.
     */
    @FXML
    public void handleBackToLauncher() {
        // Close WebSocket
        if (wsClient != null && wsClient.isConnected()) {
            wsClient.disconnect();
        }

        if (onBack != null) {
            onBack.run();
        }
    }

    /**
     * Handle leave game button (during gameplay).
     */
    @FXML
    public void handleLeaveGame() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Game");
        alert.setHeaderText("Are you sure you want to leave?");
        alert.setContentText("Your current progress will be saved before leaving.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Stop all timers
                if (questionTimeline != null) questionTimeline.stop();
                if (challengeTimeline != null) challengeTimeline.stop();
                if (gameTimeline != null) gameTimeline.stop();

                // Save score before leaving (marked as incomplete)
                saveGameScoreOnExit();

                // Close WebSocket
                if (wsClient != null && wsClient.isConnected()) {
                    wsClient.disconnect();
                }

                // Return to launcher
                if (onBack != null) {
                    onBack.run();
                }
            }
        });
    }

    /**
     * Save score when player leaves game early.
     */
    private void saveGameScoreOnExit() {
        try {
            if (scoreService == null) {
                logger.warn("ScoreService not available, skipping score save on exit");
                return;
            }

            // Only save if player has made some progress
            if (playerState.getCorrectAnswers() == 0 && playerState.getWrongAnswers() == 0) {
                logger.info("No progress to save, skipping score save on exit");
                return;
            }

            // Calculate elapsed time
            LocalDateTime endTime = LocalDateTime.now();
            int elapsedSeconds = (int) ChronoUnit.SECONDS.between(gameStartTime, endTime);

            // Create GameResult (incomplete game)
            GameResult result = new GameResult("code-breaker-multiplayer");
            result.setScore(playerState.getCredits());
            result.setMaxScore(Math.max(playerState.getCredits(), 1000));
            result.setCorrectAnswers(playerState.getCorrectAnswers());
            result.setIncorrectAnswers(playerState.getWrongAnswers());
            result.setTimeElapsedSeconds(elapsedSeconds);
            result.setCompleted(false); // Mark as incomplete
            result.setStartTime(gameStartTime);
            result.setEndTime(endTime);

            // Add custom metrics
            result.addCustomMetric("successfulHacks", playerState.getSuccessfulHacks());
            result.addCustomMetric("timesHacked", playerState.getTimesHacked());
            result.addCustomMetric("finalRank", playerState.getRank());
            result.addCustomMetric("sessionCode", sessionCode);
            result.addCustomMetric("avatarId", avatarId);
            result.addCustomMetric("exitedEarly", true);

            // Save to local database
            scoreService.saveGameScore(result);

            logger.info("Game score saved on exit: credits={}, correct={}, wrong={}, time={}s (incomplete)",
                playerState.getCredits(), playerState.getCorrectAnswers(), playerState.getWrongAnswers(), elapsedSeconds);

        } catch (Exception e) {
            logger.error("Failed to save game score on exit", e);
        }
    }

    /**
     * Start question timer.
     */
    private void startQuestionTimer(int seconds) {
        if (questionTimeline != null) {
            questionTimeline.stop();
        }

        questionTimer.setProgress(1.0);
        final int[] remaining = {seconds};

        questionTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            questionTimer.setProgress((double) remaining[0] / seconds);

            if (remaining[0] <= 0) {
                questionTimeline.stop();
                // Time's up - auto submit empty
                submitAnswer("");
            }
        }));
        questionTimeline.setCycleCount(seconds);
        questionTimeline.play();
    }

    /**
     * Start challenge timer.
     */
    private void startChallengeTimer(int seconds) {
        if (challengeTimeline != null) {
            challengeTimeline.stop();
        }

        challengeTimer.setProgress(1.0);
        final int[] remaining = {seconds};

        challengeTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            challengeTimer.setProgress((double) remaining[0] / seconds);

            if (remaining[0] <= 0) {
                challengeTimeline.stop();
                wsClient.completeMiniChallenge(false);
            }
        }));
        challengeTimeline.setCycleCount(seconds);
        challengeTimeline.play();
    }

    /**
     * Update player stats display.
     */
    private void updatePlayerStats() {
        creditsLabel.setText(String.valueOf(playerState.getCredits()));
        correctLabel.setText("Correct: " + playerState.getCorrectAnswers());
        wrongLabel.setText("Wrong: " + playerState.getWrongAnswers());
        if (playerState.getRank() > 0) {
            rankLabel.setText("Rank: #" + playerState.getRank());
        }
        shieldBox.setVisible(playerState.isShielded());
    }

    /**
     * Show a specific panel and hide others.
     */
    private void showPanel(String panel) {
        waitingPane.setVisible("waiting".equals(panel));
        questionPane.setVisible("question".equals(panel));
        rewardPane.setVisible("reward".equals(panel));
        wrongPane.setVisible("wrong".equals(panel));
        challengePane.setVisible("challenge".equals(panel));
        gameOverPane.setVisible("gameover".equals(panel));
    }

    /**
     * Add activity to the feed.
     */
    private void addActivity(String text, String color) {
        Label activity = new Label("• " + text);
        activity.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px;");
        activity.setWrapText(true);
        activity.setMaxWidth(280);

        activityBox.getChildren().add(0, activity);

        // Keep only last 20 activities
        if (activityBox.getChildren().size() > 20) {
            activityBox.getChildren().remove(20, activityBox.getChildren().size());
        }
    }

    /**
     * Get emoji for avatar ID.
     */
    private String getAvatarEmoji(String avatarId) {
        return switch (avatarId) {
            case "CLEVER_CAT" -> "🐱";
            case "MIGHTY_BEAR" -> "🐻";
            case "SHIELD_TURTLE" -> "🐢";
            case "QUICK_RABBIT" -> "🐰";
            case "WISE_OWL" -> "🦉";
            case "LUCKY_FOX" -> "🦊";
            default -> "🎮";
        };
    }
}
