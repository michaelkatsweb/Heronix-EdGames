package com.heronix.edu.games.math;

import com.heronix.edu.common.game.*;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Math Sprint - Timed arithmetic practice game
 * Students solve math problems as quickly as possible within a time limit
 */
public class MathSprintGame extends AbstractGame {
    
    private static final String GAME_ID = "math-sprint";
    private static final String GAME_NAME = "Math Sprint";
    private static final String VERSION = "1.0.0";
    
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
    private TextField answerField;
    private Button submitButton;
    private AnimationTimer timer;
    private ImageView owlSprite;
    private ImageView logoView;

    // Images loaded in setupUI() on JavaFX thread
    private Image owlImage;
    private Image logoImage;

    public MathSprintGame() {
        super();
        this.random = new Random();
        this.problems = new ArrayList<>();
        // Note: Cannot load JavaFX Image objects here - must wait for JavaFX thread in setupUI()
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
        return "Solve math problems as quickly and accurately as you can! " +
               "Practice addition, subtraction, multiplication, and division.";
    }
    
    @Override
    public GameSubject getSubject() {
        return GameSubject.MATHEMATICS;
    }
    
    @Override
    public GradeLevel[] getTargetGrades() {
        return new GradeLevel[]{
            GradeLevel.SECOND, GradeLevel.THIRD, GradeLevel.FOURTH,
            GradeLevel.FIFTH, GradeLevel.SIXTH
        };
    }
    
    @Override
    public String getVersion() {
        return VERSION;
    }
    
    @Override
    protected void onInitialize() {
        // CRITICAL: Use stderr which should ALWAYS work
        System.err.println("====== MathSprintGame.onInitialize() CALLED ======");
        System.err.println("====== MathSprintGame.onInitialize() CALLED (STDERR) ======");
        System.err.flush();

        // Also print to stdout
        System.out.println("====== MathSprintGame.onInitialize() CALLED ======");
        System.out.println("====== MathSprintGame.onInitialize() CALLED (STDERR) ======");
        System.out.flush();

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
        }

        System.out.println("Problems to solve: " + problemsToSolve);

        // Generate problems
        generateProblems();

        System.out.println("About to call Platform.runLater for setupUI");

        // Setup UI
        Platform.runLater(() -> {
            System.out.println("====== setupUI Platform.runLater EXECUTING ======");
            System.err.println("====== setupUI Platform.runLater EXECUTING (STDERR) ======");
            setupUI();
        });

        System.out.println("Platform.runLater scheduled");

        result.setMaxScore(problemsToSolve * 10); // 10 points per problem
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
            MathProblem problem = generateProblem(difficulty);
            problems.add(problem);
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
                // Addition and subtraction with small numbers
                operation = random.nextBoolean() ? 
                    MathProblem.Operation.ADDITION : MathProblem.Operation.SUBTRACTION;
                num1 = random.nextInt(20) + 1;
                num2 = random.nextInt(20) + 1;
                if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                break;
                
            case MEDIUM:
                // All operations with medium numbers
                operation = MathProblem.Operation.values()[random.nextInt(4)];
                if (operation == MathProblem.Operation.MULTIPLICATION) {
                    num1 = random.nextInt(12) + 1;
                    num2 = random.nextInt(12) + 1;
                } else if (operation == MathProblem.Operation.DIVISION) {
                    num2 = random.nextInt(12) + 1;
                    int quotient = random.nextInt(12) + 1;
                    num1 = num2 * quotient;
                } else {
                    num1 = random.nextInt(50) + 1;
                    num2 = random.nextInt(50) + 1;
                    if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                }
                break;
                
            case HARD:
                // All operations with larger numbers
                operation = MathProblem.Operation.values()[random.nextInt(4)];
                if (operation == MathProblem.Operation.MULTIPLICATION) {
                    num1 = random.nextInt(20) + 1;
                    num2 = random.nextInt(20) + 1;
                } else if (operation == MathProblem.Operation.DIVISION) {
                    num2 = random.nextInt(20) + 1;
                    int quotient = random.nextInt(20) + 1;
                    num1 = num2 * quotient;
                } else {
                    num1 = random.nextInt(100) + 1;
                    num2 = random.nextInt(100) + 1;
                    if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                }
                break;
                
            case EXPERT:
                // Complex problems
                operation = MathProblem.Operation.values()[random.nextInt(4)];
                if (operation == MathProblem.Operation.MULTIPLICATION) {
                    num1 = random.nextInt(50) + 10;
                    num2 = random.nextInt(50) + 10;
                } else if (operation == MathProblem.Operation.DIVISION) {
                    num2 = random.nextInt(50) + 10;
                    int quotient = random.nextInt(50) + 10;
                    num1 = num2 * quotient;
                } else {
                    num1 = random.nextInt(500) + 1;
                    num2 = random.nextInt(500) + 1;
                    if (operation == MathProblem.Operation.SUBTRACTION && num2 > num1) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                }
                break;
                
            default:
                num1 = 1;
                num2 = 1;
                operation = MathProblem.Operation.ADDITION;
        }
        
        return new MathProblem(num1, num2, operation);
    }
    
    /**
     * Setup the game UI
     */
    private void setupUI() {
        System.out.println("====== setupUI() METHOD STARTED ======");
        System.err.println("====== setupUI() METHOD STARTED (STDERR) ======");

        // Load images NOW on JavaFX Application Thread
        System.out.println("Loading owl image...");
        try {
            owlImage = new Image(getClass().getResourceAsStream("/com/heronix/edu/games/math/assets/owl-running.png"));
            System.out.println("Owl image loaded successfully!");
        } catch (Exception e) {
            System.err.println("Failed to load owl image: " + e.getMessage());
            e.printStackTrace();
            owlImage = null;
        }

        try {
            logoImage = new Image(getClass().getResourceAsStream("/com/heronix/edu/games/math/assets/math-quest-logo.png"));
        } catch (Exception e) {
            System.err.println("Failed to load logo image: " + e.getMessage());
            logoImage = null;
        }

        stage = new Stage();
        stage.setTitle("Math Quest");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Set colorful background
        BackgroundFill bgFill = new BackgroundFill(
            Color.rgb(240, 248, 255), // Light blue background
            CornerRadii.EMPTY,
            Insets.EMPTY
        );
        root.setBackground(new Background(bgFill));

        // Top: Logo and Score/Timer
        VBox topContainer = new VBox(10);
        topContainer.setAlignment(Pos.CENTER);

        // DEBUG: Show image loading status
        String debugInfo = "DEBUG: owlImage=" + (owlImage != null ? "LOADED" : "NULL") +
                          ", logoImage=" + (logoImage != null ? "LOADED" : "NULL");
        Label debugLabel = new Label(debugInfo);
        debugLabel.setFont(Font.font("Courier New", 12));
        debugLabel.setTextFill(Color.RED);
        debugLabel.setStyle("-fx-background-color: yellow; -fx-padding: 5px;");
        topContainer.getChildren().add(debugLabel);

        // Display Math Quest logo (pre-loaded in constructor)
        System.out.println("setupUI: logoImage is " + (logoImage != null ? "NOT NULL" : "NULL"));
        if (logoImage != null) {
            System.out.println("Creating logoView ImageView...");
            logoView = new ImageView(logoImage);
            logoView.setFitHeight(80);
            logoView.setPreserveRatio(true);
            topContainer.getChildren().add(logoView);
            System.out.println("Logo ImageView added to scene");
        } else {
            // If logo failed to load, use text label
            System.out.println("Using fallback text for logo");
            Label titleLabel = new Label("🎓 Math Quest 🎓");
            titleLabel.setFont(Font.font("Arial", 28));
            titleLabel.setTextFill(Color.rgb(25, 118, 210));
            topContainer.getChildren().add(titleLabel);
        }

        HBox statsBox = new HBox(40);
        statsBox.setAlignment(Pos.CENTER);
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", 18));
        scoreLabel.setTextFill(Color.rgb(76, 175, 80)); // Green
        timerLabel = new Label("Time: 0s");
        timerLabel.setFont(Font.font("Arial", 18));
        timerLabel.setTextFill(Color.rgb(33, 150, 243)); // Blue
        statsBox.getChildren().addAll(scoreLabel, timerLabel);

        topContainer.getChildren().add(statsBox);
        root.setTop(topContainer);

        // Center: Owl sprite, Problem and Answer
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);

        // Display owl sprite (pre-loaded in constructor)
        System.out.println("setupUI: owlImage is " + (owlImage != null ? "NOT NULL" : "NULL"));
        if (owlImage != null) {
            System.out.println("Creating owlSprite ImageView...");
            owlSprite = new ImageView(owlImage);
            owlSprite.setFitHeight(120);
            owlSprite.setPreserveRatio(true);
            centerBox.getChildren().add(owlSprite);
            System.out.println("Owl ImageView added to scene");
        } else {
            // If owl failed to load, use emoji
            System.out.println("Using fallback emoji for owl");
            Label owlLabel = new Label("🦉");
            owlLabel.setFont(Font.font(80));
            centerBox.getChildren().add(owlLabel);
        }

        problemLabel = new Label();
        problemLabel.setFont(Font.font("Arial", 42));
        problemLabel.setTextFill(Color.rgb(51, 51, 51));

        answerField = new TextField();
        answerField.setMaxWidth(250);
        answerField.setFont(Font.font("Arial", 28));
        answerField.setAlignment(Pos.CENTER);
        answerField.setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        submitButton = new Button("✓ Submit Answer");
        submitButton.setFont(Font.font("Arial", 16));
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 30px; -fx-background-radius: 8px; -fx-cursor: hand;");
        submitButton.setOnAction(e -> checkAnswer());

        // Hover effect for button
        submitButton.setOnMouseEntered(e ->
            submitButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 10px 30px; -fx-background-radius: 8px; -fx-cursor: hand;")
        );
        submitButton.setOnMouseExited(e ->
            submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 30px; -fx-background-radius: 8px; -fx-cursor: hand;")
        );

        // Allow Enter key to submit
        answerField.setOnAction(e -> checkAnswer());

        feedbackLabel = new Label();
        feedbackLabel.setFont(Font.font("Arial", 18));

        centerBox.getChildren().addAll(problemLabel, answerField, submitButton, feedbackLabel);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 700, 550);
        stage.setScene(scene);
        stage.show();

        // Focus on answer field
        answerField.requestFocus();
    }
    
    /**
     * Display a problem
     */
    private void showProblem(MathProblem problem) {
        problemLabel.setText(problem.toString());
        answerField.clear();
        answerField.requestFocus();
        feedbackLabel.setText("");
    }
    
    /**
     * Check the submitted answer
     */
    private void checkAnswer() {
        if (answerField.getText().trim().isEmpty()) {
            return;
        }
        
        try {
            int userAnswer = Integer.parseInt(answerField.getText().trim());
            MathProblem currentProblem = problems.get(currentProblemIndex);
            
            if (userAnswer == currentProblem.getAnswer()) {
                // Correct!
                result.incrementCorrectAnswers();
                result.setScore(result.getScore() + 10);
                feedbackLabel.setText("✓ Correct! Great job!");
                feedbackLabel.setTextFill(Color.rgb(76, 175, 80)); // Green

                // Animate owl - happy bounce
                if (owlSprite != null) {
                    ScaleTransition scale = new ScaleTransition(Duration.millis(200), owlSprite);
                    scale.setFromX(1.0);
                    scale.setFromY(1.0);
                    scale.setToX(1.2);
                    scale.setToY(1.2);
                    scale.setCycleCount(2);
                    scale.setAutoReverse(true);
                    scale.play();
                }
            } else {
                // Incorrect
                result.incrementIncorrectAnswers();
                feedbackLabel.setText("✗ Incorrect. The answer was: " + currentProblem.getAnswer());
                feedbackLabel.setTextFill(Color.rgb(244, 67, 54)); // Red

                // Animate owl - sad shake
                if (owlSprite != null) {
                    FadeTransition fade = new FadeTransition(Duration.millis(150), owlSprite);
                    fade.setFromValue(1.0);
                    fade.setToValue(0.5);
                    fade.setCycleCount(2);
                    fade.setAutoReverse(true);
                    fade.play();
                }
            }

            // Update score display
            scoreLabel.setText("Score: " + result.getScore());
            scoreLabel.setTextFill(Color.rgb(76, 175, 80));
            
            // Move to next problem after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> nextProblem());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (NumberFormatException e) {
            feedbackLabel.setText("Please enter a valid number");
            feedbackLabel.setStyle("-fx-text-fill: orange;");
        }
    }
    
    /**
     * Move to next problem or end game
     */
    private void nextProblem() {
        currentProblemIndex++;
        
        if (currentProblemIndex >= problems.size()) {
            // Game complete!
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
                
                // Optional: time limit
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

        // Colorful background
        BackgroundFill bgFill = new BackgroundFill(
            Color.rgb(240, 248, 255),
            CornerRadii.EMPTY,
            Insets.EMPTY
        );
        resultsBox.setBackground(new Background(bgFill));

        // Victory emoji
        Label emojiLabel = new Label("🎉");
        emojiLabel.setFont(Font.font(64));

        Label titleLabel = new Label("Math Quest Complete!");
        titleLabel.setFont(Font.font("Arial", 36));
        titleLabel.setTextFill(Color.rgb(25, 118, 210));

        // Score with color based on performance
        double percentage = (result.getScore() * 100.0) / result.getMaxScore();
        Color scoreColor = percentage >= 80 ? Color.rgb(76, 175, 80) :  // Green for 80%+
                          percentage >= 60 ? Color.rgb(255, 152, 0) :   // Orange for 60-80%
                          Color.rgb(244, 67, 54);                        // Red for <60%

        Label scoreLabel = new Label("Final Score: " + result.getScore() + " / " + result.getMaxScore());
        scoreLabel.setFont(Font.font("Arial", 28));
        scoreLabel.setTextFill(scoreColor);

        Label accuracyLabel = new Label(String.format("Accuracy: %.1f%%", result.getAccuracy()));
        accuracyLabel.setFont(Font.font("Arial", 22));
        accuracyLabel.setTextFill(Color.rgb(33, 150, 243));

        Label detailsLabel = new Label(
            "✓ Correct: " + result.getCorrectAnswers() +
            "   ✗ Incorrect: " + result.getIncorrectAnswers() +
            "   ⏱ Time: " + result.getTimeElapsedSeconds() + "s"
        );
        detailsLabel.setFont(Font.font("Arial", 16));
        detailsLabel.setTextFill(Color.rgb(96, 96, 96));

        // Performance message
        String message = percentage >= 90 ? "🌟 Outstanding! You're a math superstar!" :
                        percentage >= 80 ? "⭐ Excellent work! Keep it up!" :
                        percentage >= 70 ? "👍 Good job! Practice makes perfect!" :
                        percentage >= 60 ? "💪 Nice effort! Try again to improve!" :
                        "📚 Keep practicing! You'll get better!";

        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Arial", 18));
        messageLabel.setTextFill(Color.rgb(51, 51, 51));

        Button closeButton = new Button("Close Game");
        closeButton.setFont(Font.font("Arial", 18));
        closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 12px 40px; -fx-background-radius: 8px;");
        closeButton.setOnAction(e -> stage.close());

        // Hover effect
        closeButton.setOnMouseEntered(e ->
            closeButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-padding: 12px 40px; -fx-background-radius: 8px; -fx-cursor: hand;")
        );
        closeButton.setOnMouseExited(e ->
            closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 12px 40px; -fx-background-radius: 8px;")
        );

        resultsBox.getChildren().addAll(
            emojiLabel, titleLabel, scoreLabel, accuracyLabel, detailsLabel, messageLabel, closeButton
        );

        Scene resultsScene = new Scene(resultsBox, 700, 550);
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
                case ADDITION:
                    return num1 + num2;
                case SUBTRACTION:
                    return num1 - num2;
                case MULTIPLICATION:
                    return num1 * num2;
                case DIVISION:
                    return num1 / num2;
                default:
                    return 0;
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
