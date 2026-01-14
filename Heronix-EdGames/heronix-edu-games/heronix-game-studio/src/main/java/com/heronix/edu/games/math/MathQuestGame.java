package com.heronix.edu.games.math;

import com.heronix.edu.common.game.*;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Math Quest - An exciting math adventure game with colorful visuals
 * Students solve math problems with an animated owl companion
 */
public class MathQuestGame extends AbstractGame {

    private static final String GAME_ID = "math-quest";
    private static final String GAME_NAME = "Math Quest";
    private static final String VERSION = "1.0.0";

    // Game state
    private List<MathProblem> problems;
    private int currentProblemIndex;
    private int problemsToSolve;
    private Random random;

    // UI Components
    private Stage stage;
    private Label problemLabel;
    private Label scoreLabel;
    private Label timerLabel;
    private Label feedbackLabel;
    private Label progressLabel;
    private TextField answerField;
    private Button submitButton;
    private AnimationTimer timer;
    private ImageView owlSprite;
    private VBox rootContainer;

    // Images
    private Image owlImage;

    public MathQuestGame() {
        super();
        this.random = new Random();
        this.problems = new ArrayList<>();
    }

    @Override
    public String getGameId() {
        return GAME_ID;
    }

    @Override
    public String getName() {
        return GAME_NAME;
    }

    @Override
    public String getDescription() {
        return "Embark on a math adventure! Solve problems with your owl companion.";
    }

    @Override
    public GameSubject getSubject() {
        return GameSubject.MATHEMATICS;
    }

    @Override
    public GradeLevel[] getTargetGrades() {
        return new GradeLevel[]{
            GradeLevel.THIRD, GradeLevel.FOURTH,
            GradeLevel.FIFTH, GradeLevel.SIXTH
        };
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    protected void onInitialize() {
        // Determine problem count based on difficulty
        switch (context.getDifficultyLevel()) {
            case EASY:
                problemsToSolve = 10;
                break;
            case MEDIUM:
                problemsToSolve = 15;
                break;
            case HARD:
                problemsToSolve = 20;
                break;
            case EXPERT:
                problemsToSolve = 25;
                break;
            default:
                problemsToSolve = 10;
        }

        // Generate problems
        generateProblems();

        // Setup UI on JavaFX thread
        Platform.runLater(this::setupUI);

        result.setMaxScore(problemsToSolve * 10);
        result.addCustomMetric("totalQuestions", problemsToSolve);
    }

    @Override
    protected void onStart() {
        currentProblemIndex = 0;
        Platform.runLater(() -> {
            showProblem(problems.get(currentProblemIndex));
            startTimer();
        });
    }

    @Override
    protected void onPause() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    protected void onResume() {
        if (timer != null) {
            timer.start();
        }
    }

    @Override
    protected void onStop() {
        if (timer != null) {
            timer.stop();
        }
        Platform.runLater(this::showResults);
    }

    @Override
    protected void saveStateData(GameState state) {
        state.setCurrentQuestion(currentProblemIndex);
        state.putData("correctAnswers", result.getCorrectAnswers());
        state.putData("incorrectAnswers", result.getIncorrectAnswers());
    }

    @Override
    protected void loadStateData(GameState state) {
        currentProblemIndex = state.getCurrentQuestion();
        result.setCorrectAnswers(state.getData("correctAnswers", Integer.class));
        result.setIncorrectAnswers(state.getData("incorrectAnswers", Integer.class));
        if (currentProblemIndex < problems.size()) {
            showProblem(problems.get(currentProblemIndex));
        }
    }

    /**
     * Generate math problems based on difficulty level
     */
    private void generateProblems() {
        problems.clear();
        DifficultyLevel difficulty = context.getDifficultyLevel();

        for (int i = 0; i < problemsToSolve; i++) {
            problems.add(generateProblem(difficulty));
        }
    }

    /**
     * Generate a single math problem
     */
    private MathProblem generateProblem(DifficultyLevel difficulty) {
        MathProblem.Operation operation;
        int num1, num2;

        switch (difficulty) {
            case EASY:
                operation = random.nextBoolean() ?
                    MathProblem.Operation.ADDITION : MathProblem.Operation.SUBTRACTION;
                num1 = random.nextInt(20) + 1;
                num2 = random.nextInt(20) + 1;
                if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                    int temp = num1; num1 = num2; num2 = temp;
                }
                break;

            case MEDIUM:
                operation = MathProblem.Operation.values()[random.nextInt(4)];
                if (operation == MathProblem.Operation.MULTIPLICATION) {
                    num1 = random.nextInt(12) + 1;
                    num2 = random.nextInt(12) + 1;
                } else if (operation == MathProblem.Operation.DIVISION) {
                    num2 = random.nextInt(12) + 1;
                    num1 = num2 * (random.nextInt(12) + 1);
                } else {
                    num1 = random.nextInt(50) + 1;
                    num2 = random.nextInt(50) + 1;
                    if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                        int temp = num1; num1 = num2; num2 = temp;
                    }
                }
                break;

            case HARD:
                operation = MathProblem.Operation.values()[random.nextInt(4)];
                if (operation == MathProblem.Operation.MULTIPLICATION) {
                    num1 = random.nextInt(20) + 1;
                    num2 = random.nextInt(20) + 1;
                } else if (operation == MathProblem.Operation.DIVISION) {
                    num2 = random.nextInt(20) + 1;
                    num1 = num2 * (random.nextInt(20) + 1);
                } else {
                    num1 = random.nextInt(100) + 1;
                    num2 = random.nextInt(100) + 1;
                    if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                        int temp = num1; num1 = num2; num2 = temp;
                    }
                }
                break;

            default: // EXPERT
                operation = MathProblem.Operation.values()[random.nextInt(4)];
                if (operation == MathProblem.Operation.MULTIPLICATION) {
                    num1 = random.nextInt(50) + 10;
                    num2 = random.nextInt(50) + 10;
                } else if (operation == MathProblem.Operation.DIVISION) {
                    num2 = random.nextInt(50) + 10;
                    num1 = num2 * (random.nextInt(50) + 10);
                } else {
                    num1 = random.nextInt(500) + 1;
                    num2 = random.nextInt(500) + 1;
                    if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                        int temp = num1; num1 = num2; num2 = temp;
                    }
                }
        }

        return new MathProblem(num1, num2, operation);
    }

    /**
     * Setup the game UI with colorful visuals
     */
    private void setupUI() {
        // Load owl image from resources
        loadImages();

        stage = new Stage();
        stage.setTitle("Math Quest - Adventure Awaits!");

        rootContainer = new VBox(15);
        rootContainer.setPadding(new Insets(25));
        rootContainer.setAlignment(Pos.CENTER);

        // Create gradient background
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(102, 126, 234)),    // Purple-blue
            new Stop(0.5, Color.rgb(118, 75, 162)),   // Purple
            new Stop(1, Color.rgb(240, 147, 251))     // Pink
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        rootContainer.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // === HEADER SECTION ===
        VBox headerBox = createHeaderSection();

        // === GAME AREA ===
        VBox gameArea = createGameArea();

        // === FOOTER with Submit Button ===
        HBox footerBox = createFooterSection();

        rootContainer.getChildren().addAll(headerBox, gameArea, footerBox);

        Scene scene = new Scene(rootContainer, 750, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        answerField.requestFocus();
    }

    /**
     * Load images from resources
     */
    private void loadImages() {
        try {
            InputStream owlStream = getClass().getResourceAsStream("/images/owl-sprite.png");
            if (owlStream != null) {
                owlImage = new Image(owlStream);
            }
        } catch (Exception e) {
            owlImage = null;
        }
    }

    /**
     * Create the header section with title and stats
     */
    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        // Game Title
        Label titleLabel = new Label("MATH QUEST");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 2, 2);");

        // Stats Row
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreLabel.setTextFill(Color.rgb(255, 215, 0)); // Gold

        timerLabel = new Label("Time: 0s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        timerLabel.setTextFill(Color.WHITE);

        progressLabel = new Label("Question: 1/" + problemsToSolve);
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        progressLabel.setTextFill(Color.rgb(144, 238, 144)); // Light green

        statsBox.getChildren().addAll(scoreLabel, timerLabel, progressLabel);

        header.getChildren().addAll(titleLabel, statsBox);
        return header;
    }

    /**
     * Create the main game area with owl and problem
     */
    private VBox createGameArea() {
        VBox gameArea = new VBox(20);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(20));
        gameArea.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20;");
        VBox.setVgrow(gameArea, Priority.ALWAYS);

        // Owl Character (or emoji fallback)
        if (owlImage != null && !owlImage.isError()) {
            owlSprite = new ImageView(owlImage);
            owlSprite.setFitHeight(100);
            owlSprite.setPreserveRatio(true);
            gameArea.getChildren().add(owlSprite);
        } else {
            Label owlEmoji = new Label("🦉");
            owlEmoji.setFont(Font.font(70));
            gameArea.getChildren().add(owlEmoji);
        }

        // Problem Display
        problemLabel = new Label("Loading...");
        problemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        problemLabel.setTextFill(Color.rgb(51, 51, 51));

        // Answer Input
        answerField = new TextField();
        answerField.setMaxWidth(200);
        answerField.setFont(Font.font("Arial", 28));
        answerField.setAlignment(Pos.CENTER);
        answerField.setPromptText("?");
        answerField.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #667eea; " +
            "-fx-border-width: 3px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px;"
        );
        answerField.setOnAction(e -> checkAnswer());

        // Feedback Label
        feedbackLabel = new Label("");
        feedbackLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        feedbackLabel.setMinHeight(30);

        gameArea.getChildren().addAll(problemLabel, answerField, feedbackLabel);
        return gameArea;
    }

    /**
     * Create the footer section with submit button
     */
    private HBox createFooterSection() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15, 0, 0, 0));

        submitButton = new Button("Submit Answer");
        submitButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        submitButton.setPrefWidth(200);
        submitButton.setPrefHeight(50);
        submitButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 2, 2);"
        );
        submitButton.setOnAction(e -> checkAnswer());

        // Hover effects
        submitButton.setOnMouseEntered(e -> submitButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #45a049, #3d8b40); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 2, 3);"
        ));
        submitButton.setOnMouseExited(e -> submitButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 2, 2);"
        ));

        footer.getChildren().add(submitButton);
        return footer;
    }

    /**
     * Display a problem
     */
    private void showProblem(MathProblem problem) {
        problemLabel.setText(problem.toString());
        answerField.clear();
        answerField.requestFocus();
        feedbackLabel.setText("");
        progressLabel.setText("Question: " + (currentProblemIndex + 1) + "/" + problemsToSolve);
    }

    /**
     * Check the submitted answer
     */
    private void checkAnswer() {
        String answerText = answerField.getText().trim();
        if (answerText.isEmpty()) {
            return;
        }

        try {
            int userAnswer = Integer.parseInt(answerText);
            MathProblem currentProblem = problems.get(currentProblemIndex);

            if (userAnswer == currentProblem.getAnswer()) {
                // Correct answer
                result.incrementCorrectAnswers();
                result.setScore(result.getScore() + 10);
                feedbackLabel.setText("Correct! Great job!");
                feedbackLabel.setTextFill(Color.rgb(76, 175, 80));

                // Animate owl
                animateOwlHappy();
            } else {
                // Wrong answer
                result.incrementIncorrectAnswers();
                feedbackLabel.setText("Oops! Answer was: " + currentProblem.getAnswer());
                feedbackLabel.setTextFill(Color.rgb(244, 67, 54));

                // Shake animation could go here
            }

            // Update score
            scoreLabel.setText("Score: " + result.getScore());

            // Disable input temporarily
            answerField.setDisable(true);
            submitButton.setDisable(true);

            // Move to next problem after delay
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    Platform.runLater(() -> {
                        answerField.setDisable(false);
                        submitButton.setDisable(false);
                        nextProblem();
                    });
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }).start();

        } catch (NumberFormatException e) {
            feedbackLabel.setText("Please enter a number!");
            feedbackLabel.setTextFill(Color.rgb(255, 152, 0));
        }
    }

    /**
     * Animate owl on correct answer
     */
    private void animateOwlHappy() {
        if (owlSprite != null) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), owlSprite);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(1.15);
            scale.setToY(1.15);
            scale.setCycleCount(2);
            scale.setAutoReverse(true);
            scale.play();
        }
    }

    /**
     * Move to next problem or end game
     */
    private void nextProblem() {
        currentProblemIndex++;

        if (currentProblemIndex >= problems.size()) {
            stop();
        } else {
            showProblem(problems.get(currentProblemIndex));
        }
    }

    /**
     * Start the game timer
     */
    private void startTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateElapsedTime();
                timerLabel.setText("Time: " + result.getTimeElapsedSeconds() + "s");

                int timeLimit = context.getTimeLimit();
                if (timeLimit > 0 && result.getTimeElapsedSeconds() >= timeLimit) {
                    stop();
                }
            }
        };
        timer.start();
    }

    /**
     * Show results screen
     */
    private void showResults() {
        VBox resultsBox = new VBox(20);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setPadding(new Insets(40));

        // Gradient background
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(102, 126, 234)),
            new Stop(1, Color.rgb(118, 75, 162))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        resultsBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Trophy/Stars based on performance
        double percentage = (result.getScore() * 100.0) / result.getMaxScore();
        String emoji = percentage >= 90 ? "🏆" : percentage >= 70 ? "⭐" : percentage >= 50 ? "👍" : "📚";

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(80));

        Label titleLabel = new Label("Quest Complete!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        titleLabel.setTextFill(Color.WHITE);

        // Score card
        VBox scoreCard = new VBox(15);
        scoreCard.setAlignment(Pos.CENTER);
        scoreCard.setPadding(new Insets(25));
        scoreCard.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15;");
        scoreCard.setMaxWidth(400);

        Label finalScore = new Label("Final Score: " + result.getScore() + "/" + result.getMaxScore());
        finalScore.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        finalScore.setTextFill(percentage >= 70 ? Color.rgb(76, 175, 80) : Color.rgb(244, 67, 54));

        Label accuracyLabel = new Label(String.format("Accuracy: %.0f%%", result.getAccuracy()));
        accuracyLabel.setFont(Font.font("Arial", 22));
        accuracyLabel.setTextFill(Color.rgb(33, 150, 243));

        Label detailsLabel = new Label(
            "Correct: " + result.getCorrectAnswers() +
            "  |  Wrong: " + result.getIncorrectAnswers() +
            "  |  Time: " + result.getTimeElapsedSeconds() + "s"
        );
        detailsLabel.setFont(Font.font("Arial", 16));
        detailsLabel.setTextFill(Color.rgb(100, 100, 100));

        // Performance message
        String message = percentage >= 90 ? "Outstanding! You're a Math Champion!" :
                        percentage >= 70 ? "Great work! Keep practicing!" :
                        percentage >= 50 ? "Good effort! Try again to improve!" :
                        "Don't give up! Practice makes perfect!";

        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        messageLabel.setTextFill(Color.rgb(80, 80, 80));

        scoreCard.getChildren().addAll(finalScore, accuracyLabel, detailsLabel, messageLabel);

        // Close button
        Button closeButton = new Button("Close Game");
        closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        closeButton.setPrefWidth(180);
        closeButton.setPrefHeight(45);
        closeButton.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: #667eea; " +
            "-fx-background-radius: 22; " +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> stage.close());

        resultsBox.getChildren().addAll(emojiLabel, titleLabel, scoreCard, closeButton);

        Scene resultsScene = new Scene(resultsBox, 750, 600);
        stage.setScene(resultsScene);
    }

    /**
     * Inner class representing a math problem
     */
    private static class MathProblem {
        enum Operation {
            ADDITION("+"),
            SUBTRACTION("-"),
            MULTIPLICATION("×"),
            DIVISION("÷");

            private final String symbol;

            Operation(String symbol) {
                this.symbol = symbol;
            }

            public String getSymbol() {
                return symbol;
            }
        }

        private final int num1;
        private final int num2;
        private final Operation operation;
        private final int answer;

        public MathProblem(int num1, int num2, Operation operation) {
            this.num1 = num1;
            this.num2 = num2;
            this.operation = operation;
            this.answer = calculateAnswer();
        }

        private int calculateAnswer() {
            switch (operation) {
                case ADDITION: return num1 + num2;
                case SUBTRACTION: return num1 - num2;
                case MULTIPLICATION: return num1 * num2;
                case DIVISION: return num1 / num2;
                default: return 0;
            }
        }

        public int getAnswer() {
            return answer;
        }

        @Override
        public String toString() {
            return num1 + " " + operation.getSymbol() + " " + num2 + " = ?";
        }
    }
}
