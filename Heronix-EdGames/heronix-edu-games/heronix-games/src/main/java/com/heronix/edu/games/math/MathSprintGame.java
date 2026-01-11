package com.heronix.edu.games.math;

import com.heronix.edu.common.game.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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
    
    public MathSprintGame() {
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
        
        // Generate problems
        generateProblems();
        
        // Setup UI
        Platform.runLater(this::setupUI);
        
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
        stage = new Stage();
        stage.setTitle(GAME_NAME);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Top: Score and Timer
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER);
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font(18));
        timerLabel = new Label("Time: 0s");
        timerLabel.setFont(Font.font(18));
        topBox.getChildren().addAll(scoreLabel, timerLabel);
        root.setTop(topBox);
        
        // Center: Problem and Answer
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        
        problemLabel = new Label();
        problemLabel.setFont(Font.font(36));
        
        answerField = new TextField();
        answerField.setMaxWidth(200);
        answerField.setFont(Font.font(24));
        answerField.setAlignment(Pos.CENTER);
        
        submitButton = new Button("Submit");
        submitButton.setFont(Font.font(18));
        submitButton.setOnAction(e -> checkAnswer());
        
        // Allow Enter key to submit
        answerField.setOnAction(e -> checkAnswer());
        
        feedbackLabel = new Label();
        feedbackLabel.setFont(Font.font(16));
        
        centerBox.getChildren().addAll(problemLabel, answerField, submitButton, feedbackLabel);
        root.setCenter(centerBox);
        
        Scene scene = new Scene(root, 600, 400);
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
                feedbackLabel.setText("✓ Correct!");
                feedbackLabel.setStyle("-fx-text-fill: green;");
            } else {
                // Incorrect
                result.incrementIncorrectAnswers();
                feedbackLabel.setText("✗ Incorrect. Answer: " + currentProblem.getAnswer());
                feedbackLabel.setStyle("-fx-text-fill: red;");
            }
            
            // Update score display
            scoreLabel.setText("Score: " + result.getScore());
            
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
        VBox resultsBox = new VBox(15);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setPadding(new Insets(30));
        
        Label titleLabel = new Label("Game Complete!");
        titleLabel.setFont(Font.font(32));
        
        Label scoreLabel = new Label("Final Score: " + result.getScore() + " / " + result.getMaxScore());
        scoreLabel.setFont(Font.font(24));
        
        Label accuracyLabel = new Label(String.format("Accuracy: %.1f%%", result.getAccuracy()));
        accuracyLabel.setFont(Font.font(20));
        
        Label detailsLabel = new Label(
            "Correct: " + result.getCorrectAnswers() + 
            " | Incorrect: " + result.getIncorrectAnswers() +
            " | Time: " + result.getTimeElapsedSeconds() + "s"
        );
        detailsLabel.setFont(Font.font(16));
        
        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font(18));
        closeButton.setOnAction(e -> stage.close());
        
        resultsBox.getChildren().addAll(
            titleLabel, scoreLabel, accuracyLabel, detailsLabel, closeButton
        );
        
        Scene resultsScene = new Scene(resultsBox, 600, 400);
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
