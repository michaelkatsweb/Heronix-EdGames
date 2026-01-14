package com.heronix.edu.games.language;

import com.heronix.edu.common.game.*;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Spelling Tetris - A Tetris-style spelling game
 * Letters fall from the top and students must arrange them to spell words.
 * When a valid word is formed horizontally, it clears and scores points.
 */
public class SpellingTetrisGame extends AbstractGame {

    private static final String GAME_ID = "spelling-tetris";
    private static final String GAME_NAME = "Spelling Tetris";
    private static final String VERSION = "1.0.0";

    // Game board dimensions
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 12;  // Reduced for better fit
    private static final int CELL_SIZE = 32;     // Smaller cells for responsiveness

    // Word lists by difficulty
    private static final String[] EASY_WORDS = {
        "CAT", "DOG", "SUN", "RUN", "BIG", "RED", "HAT", "CUP", "BED", "PEN",
        "TOP", "BAT", "MAP", "RAT", "FAN", "JAM", "VAN", "HOP", "POP", "MOP"
    };

    private static final String[] MEDIUM_WORDS = {
        "HAPPY", "WATER", "FRIEND", "SCHOOL", "GARDEN", "YELLOW", "FAMILY",
        "PEOPLE", "WEATHER", "KITCHEN", "ANIMAL", "PLANET", "FLOWER", "ORANGE"
    };

    private static final String[] HARD_WORDS = {
        "BEAUTIFUL", "DIFFERENT", "IMPORTANT", "EDUCATION", "ADVENTURE",
        "CELEBRATE", "CHALLENGE", "KNOWLEDGE", "NECESSARY", "RESPONSIBLE"
    };

    // Game state
    private char[][] board;
    private Color[][] boardColors;
    private List<String> targetWords;
    private Set<String> completedWords;
    private FallingLetter currentLetter;
    private Random random;
    private int wordsToComplete;
    private int currentWordIndex;
    private String currentTargetWord;

    // UI Components
    private Stage stage;
    private GridPane gameGrid;
    private Label scoreLabel;
    private Label levelLabel;
    private Label targetWordLabel;
    private Label hintLabel;
    private Label wordsRemainingLabel;
    private VBox nextLettersBox;
    private Timeline gameLoop;
    private AnimationTimer timer;
    private boolean gameOver;

    // Game speed (milliseconds per drop)
    private long dropInterval;
    private long lastDropTime;

    // Letter queue
    private Queue<Character> letterQueue;
    private static final int LETTER_QUEUE_SIZE = 5;

    // Colors for letters
    private static final Color[] LETTER_COLORS = {
        Color.rgb(231, 76, 60),   // Red
        Color.rgb(46, 204, 113),  // Green
        Color.rgb(52, 152, 219),  // Blue
        Color.rgb(155, 89, 182),  // Purple
        Color.rgb(241, 196, 15),  // Yellow
        Color.rgb(230, 126, 34),  // Orange
        Color.rgb(26, 188, 156),  // Teal
        Color.rgb(236, 240, 241)  // Light gray
    };

    public SpellingTetrisGame() {
        super();
        this.random = new Random();
        this.board = new char[BOARD_HEIGHT][BOARD_WIDTH];
        this.boardColors = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        this.targetWords = new ArrayList<>();
        this.completedWords = new HashSet<>();
        this.letterQueue = new LinkedList<>();
        this.gameOver = false;
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
        return "Catch falling letters and arrange them to spell words! Like Tetris, but with spelling!";
    }

    @Override
    public GameSubject getSubject() {
        return GameSubject.LANGUAGE_ARTS;
    }

    @Override
    public GradeLevel[] getTargetGrades() {
        return new GradeLevel[]{
            GradeLevel.SECOND, GradeLevel.THIRD,
            GradeLevel.FOURTH, GradeLevel.FIFTH
        };
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    protected void onInitialize() {
        // Clear board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = ' ';
                boardColors[row][col] = null;
            }
        }

        // Select word list and settings based on difficulty
        String[] sourceWords;
        switch (context.getDifficultyLevel()) {
            case EASY:
                sourceWords = EASY_WORDS;
                wordsToComplete = 5;
                dropInterval = 1200; // Slower
                break;
            case MEDIUM:
                sourceWords = MEDIUM_WORDS;
                wordsToComplete = 7;
                dropInterval = 900;
                break;
            case HARD:
            default:
                sourceWords = HARD_WORDS;
                wordsToComplete = 10;
                dropInterval = 600; // Faster
                break;
        }

        // Shuffle and select target words
        List<String> shuffled = new ArrayList<>(Arrays.asList(sourceWords));
        Collections.shuffle(shuffled, random);
        targetWords = shuffled.subList(0, Math.min(wordsToComplete, shuffled.size()));

        currentWordIndex = 0;
        currentTargetWord = targetWords.get(currentWordIndex);

        // Initialize letter queue with letters from current word
        initializeLetterQueue();

        Platform.runLater(this::setupUI);

        result.setMaxScore(wordsToComplete * 100);
        result.addCustomMetric("totalWords", wordsToComplete);
    }

    private void initializeLetterQueue() {
        letterQueue.clear();
        // Add letters from current target word, shuffled
        List<Character> letters = new ArrayList<>();
        for (char c : currentTargetWord.toCharArray()) {
            letters.add(c);
        }
        // Add some random extra letters to increase challenge
        int extraLetters = Math.max(2, LETTER_QUEUE_SIZE - letters.size());
        for (int i = 0; i < extraLetters; i++) {
            letters.add((char) ('A' + random.nextInt(26)));
        }
        Collections.shuffle(letters, random);

        for (int i = 0; i < Math.min(LETTER_QUEUE_SIZE, letters.size()); i++) {
            letterQueue.add(letters.get(i));
        }
    }

    @Override
    protected void onStart() {
        gameOver = false;
        currentLetter = null;
        lastDropTime = System.currentTimeMillis();

        Platform.runLater(() -> {
            spawnNewLetter();
            startGameLoop();
            startTimer();
        });
    }

    @Override
    protected void onPause() {
        if (gameLoop != null) gameLoop.pause();
        if (timer != null) timer.stop();
    }

    @Override
    protected void onResume() {
        if (gameLoop != null) gameLoop.play();
        if (timer != null) timer.start();
    }

    @Override
    protected void onStop() {
        if (gameLoop != null) gameLoop.stop();
        if (timer != null) timer.stop();
        Platform.runLater(this::showResults);
    }

    @Override
    protected void saveStateData(GameState state) {
        state.setCurrentQuestion(currentWordIndex);
    }

    @Override
    protected void loadStateData(GameState state) {
        currentWordIndex = state.getCurrentQuestion();
    }

    private void setupUI() {
        stage = new Stage();
        stage.setTitle("Spelling Tetris - Word Challenge!");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Gradient background (purple/blue theme)
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(102, 51, 153)),
            new Stop(0.5, Color.rgb(75, 0, 130)),
            new Stop(1, Color.rgb(48, 25, 100))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Top: Title and stats (compact)
        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(5));

        Label titleLabel = new Label("SPELLING TETRIS");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(5));
        statsBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        scoreLabel.setTextFill(Color.WHITE);

        levelLabel = new Label("Level: " + (currentWordIndex + 1));
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        levelLabel.setTextFill(Color.WHITE);

        wordsRemainingLabel = new Label("Words: 0/" + wordsToComplete);
        wordsRemainingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        wordsRemainingLabel.setTextFill(Color.WHITE);

        statsBox.getChildren().addAll(scoreLabel, levelLabel, wordsRemainingLabel);
        topBox.getChildren().addAll(titleLabel, statsBox);
        root.setTop(topBox);

        // Center: Game grid
        gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 5;");
        gameGrid.setPadding(new Insets(3));

        // Initialize grid cells
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                StackPane cell = createCell(' ', null);
                gameGrid.add(cell, col, row);
            }
        }

        root.setCenter(gameGrid);

        // Right: Target word and next letters (compact)
        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setPadding(new Insets(10));
        rightBox.setMinWidth(160);
        rightBox.setMaxWidth(180);
        rightBox.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10;");

        Label targetLabel = new Label("SPELL THIS:");
        targetLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        targetLabel.setTextFill(Color.LIGHTGRAY);

        targetWordLabel = new Label(currentTargetWord);
        targetWordLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        targetWordLabel.setTextFill(Color.YELLOW);

        hintLabel = new Label("(" + currentTargetWord.length() + " letters)");
        hintLabel.setFont(Font.font("Arial", 10));
        hintLabel.setTextFill(Color.LIGHTGRAY);

        Label nextLabel = new Label("NEXT:");
        nextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nextLabel.setTextFill(Color.LIGHTGRAY);

        nextLettersBox = new VBox(3);
        nextLettersBox.setAlignment(Pos.CENTER);
        updateNextLettersDisplay();

        Label controlsLabel = new Label("CONTROLS:");
        controlsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        controlsLabel.setTextFill(Color.LIGHTGRAY);

        Label arrowsLabel = new Label("\u2190\u2192 Move | \u2193 Drop\nSPACE Instant");
        arrowsLabel.setFont(Font.font("Arial", 10));
        arrowsLabel.setTextFill(Color.WHITE);
        arrowsLabel.setStyle("-fx-text-alignment: center;");

        rightBox.getChildren().addAll(
            targetLabel, targetWordLabel, hintLabel,
            nextLabel, nextLettersBox,
            controlsLabel, arrowsLabel
        );
        root.setRight(rightBox);

        // Calculate appropriate window size based on board
        int windowWidth = (BOARD_WIDTH * CELL_SIZE) + 220; // grid + side panel + padding
        int windowHeight = (BOARD_HEIGHT * CELL_SIZE) + 120; // grid + top panel + padding

        Scene scene = new Scene(root, windowWidth, windowHeight);

        // Key controls
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(450);
        stage.show();
    }

    private StackPane createCell(char letter, Color bgColor) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);

        Rectangle rect = new Rectangle(CELL_SIZE - 2, CELL_SIZE - 2);
        rect.setArcWidth(6);
        rect.setArcHeight(6);

        if (bgColor != null) {
            rect.setFill(bgColor);
            rect.setStroke(bgColor.darker());
        } else {
            rect.setFill(Color.rgb(30, 30, 50, 0.5));
            rect.setStroke(Color.rgb(60, 60, 80));
        }
        rect.setStrokeWidth(1.5);

        Text text = new Text(letter == ' ' ? "" : String.valueOf(letter));
        text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);

        cell.getChildren().addAll(rect, text);
        return cell;
    }

    private void updateNextLettersDisplay() {
        nextLettersBox.getChildren().clear();
        int count = 0;
        for (Character c : letterQueue) {
            if (count >= 4) break;
            StackPane letterCell = createCell(c, LETTER_COLORS[count % LETTER_COLORS.length]);
            letterCell.setPrefSize(28, 28);
            letterCell.setMinSize(28, 28);
            letterCell.setMaxSize(28, 28);
            nextLettersBox.getChildren().add(letterCell);
            count++;
        }
    }

    private void handleKeyPress(KeyCode code) {
        if (gameOver || currentLetter == null) return;

        switch (code) {
            case LEFT:
                moveLetter(-1);
                break;
            case RIGHT:
                moveLetter(1);
                break;
            case DOWN:
                dropLetterOneStep();
                break;
            case SPACE:
                dropLetterInstant();
                break;
            default:
                // Ignore other keys
                break;
        }
    }

    private void moveLetter(int direction) {
        if (currentLetter == null) return;

        int newCol = currentLetter.col + direction;
        if (newCol >= 0 && newCol < BOARD_WIDTH &&
            board[currentLetter.row][newCol] == ' ') {
            currentLetter.col = newCol;
            renderBoard();
        }
    }

    private void dropLetterOneStep() {
        if (currentLetter == null) return;

        if (canMoveTo(currentLetter.row + 1, currentLetter.col)) {
            currentLetter.row++;
            renderBoard();
        } else {
            placeLetter();
        }
    }

    private void dropLetterInstant() {
        if (currentLetter == null) return;

        while (canMoveTo(currentLetter.row + 1, currentLetter.col)) {
            currentLetter.row++;
        }
        placeLetter();
        renderBoard();
    }

    private boolean canMoveTo(int row, int col) {
        return row >= 0 && row < BOARD_HEIGHT &&
               col >= 0 && col < BOARD_WIDTH &&
               board[row][col] == ' ';
    }

    private void placeLetter() {
        if (currentLetter == null) return;

        board[currentLetter.row][currentLetter.col] = currentLetter.letter;
        boardColors[currentLetter.row][currentLetter.col] = currentLetter.color;

        // Check for completed words
        checkForWords();

        // Spawn new letter
        spawnNewLetter();
    }

    private void checkForWords() {
        // Check each row for the target word
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            StringBuilder rowStr = new StringBuilder();
            for (int col = 0; col < BOARD_WIDTH; col++) {
                rowStr.append(board[row][col]);
            }

            String rowContent = rowStr.toString();
            if (rowContent.contains(currentTargetWord)) {
                // Word found! Clear it and score
                int startIndex = rowContent.indexOf(currentTargetWord);
                clearWord(row, startIndex, currentTargetWord.length());
                wordCompleted();
            }
        }
    }

    private void clearWord(int row, int startCol, int length) {
        // Flash animation would go here
        // Clear the letters
        for (int col = startCol; col < startCol + length && col < BOARD_WIDTH; col++) {
            board[row][col] = ' ';
            boardColors[row][col] = null;
        }

        // Drop letters above
        dropLettersAbove(row);

        renderBoard();
    }

    private void dropLettersAbove(int clearedRow) {
        // For each column, drop letters above the cleared row
        for (int col = 0; col < BOARD_WIDTH; col++) {
            for (int row = clearedRow; row > 0; row--) {
                board[row][col] = board[row - 1][col];
                boardColors[row][col] = boardColors[row - 1][col];
            }
            board[0][col] = ' ';
            boardColors[0][col] = null;
        }
    }

    private void wordCompleted() {
        result.incrementCorrectAnswers();
        result.setScore(result.getScore() + currentTargetWord.length() * 20);
        completedWords.add(currentTargetWord);

        currentWordIndex++;

        Platform.runLater(() -> {
            scoreLabel.setText("Score: " + result.getScore());
            wordsRemainingLabel.setText("Words: " + currentWordIndex + "/" + wordsToComplete);
            levelLabel.setText("Level: " + (currentWordIndex + 1));
        });

        if (currentWordIndex >= targetWords.size()) {
            // All words completed - win!
            gameOver = true;
            stop();
        } else {
            // Next word
            currentTargetWord = targetWords.get(currentWordIndex);
            initializeLetterQueue();

            Platform.runLater(() -> {
                targetWordLabel.setText(currentTargetWord);
                hintLabel.setText("(" + currentTargetWord.length() + " letters)");
                updateNextLettersDisplay();
            });

            // Increase speed slightly
            dropInterval = Math.max(300, dropInterval - 50);
        }
    }

    private void spawnNewLetter() {
        // Check if game over (top row blocked)
        int spawnCol = BOARD_WIDTH / 2;
        if (board[0][spawnCol] != ' ') {
            gameOver = true;
            stop();
            return;
        }

        // Get next letter from queue
        char letter;
        if (!letterQueue.isEmpty()) {
            letter = letterQueue.poll();
        } else {
            // Queue empty, refill with mix of target word letters and random
            refillLetterQueue();
            letter = letterQueue.poll();
        }

        Color color = LETTER_COLORS[random.nextInt(LETTER_COLORS.length)];
        currentLetter = new FallingLetter(letter, 0, spawnCol, color);

        Platform.runLater(this::updateNextLettersDisplay);
        renderBoard();
    }

    private void refillLetterQueue() {
        List<Character> letters = new ArrayList<>();

        // Add letters from current target word
        for (char c : currentTargetWord.toCharArray()) {
            letters.add(c);
        }

        // Add some random letters
        for (int i = 0; i < 3; i++) {
            letters.add((char) ('A' + random.nextInt(26)));
        }

        Collections.shuffle(letters, random);

        for (Character c : letters) {
            if (letterQueue.size() < LETTER_QUEUE_SIZE) {
                letterQueue.add(c);
            }
        }
    }

    private void renderBoard() {
        Platform.runLater(() -> {
            gameGrid.getChildren().clear();

            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    char letter = board[row][col];
                    Color color = boardColors[row][col];

                    // Check if current falling letter is here
                    if (currentLetter != null &&
                        currentLetter.row == row &&
                        currentLetter.col == col) {
                        letter = currentLetter.letter;
                        color = currentLetter.color;
                    }

                    StackPane cell = createCell(letter, color);
                    gameGrid.add(cell, col, row);
                }
            }
        });
    }

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            if (gameOver || isPaused) return;

            long now = System.currentTimeMillis();
            if (now - lastDropTime >= dropInterval) {
                lastDropTime = now;
                dropLetterOneStep();
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void startTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateElapsedTime();
            }
        };
        timer.start();
    }

    private void showResults() {
        VBox resultsBox = new VBox(15);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setPadding(new Insets(20));

        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(102, 51, 153)),
            new Stop(1, Color.rgb(48, 25, 100))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        resultsBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        double percentage = completedWords.size() * 100.0 / wordsToComplete;
        String emoji = percentage >= 90 ? "🏆" : percentage >= 70 ? "⭐" : percentage >= 50 ? "👍" : "📚";

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(60));

        String titleText = completedWords.size() == wordsToComplete ?
            "Congratulations!" : "Game Over!";
        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);

        VBox scoreCard = new VBox(10);
        scoreCard.setAlignment(Pos.CENTER);
        scoreCard.setPadding(new Insets(15));
        scoreCard.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 12;");
        scoreCard.setMaxWidth(350);

        Label finalScore = new Label("Score: " + result.getScore());
        finalScore.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        finalScore.setTextFill(percentage >= 70 ? Color.rgb(76, 175, 80) : Color.rgb(244, 67, 54));

        Label wordsLabel = new Label("Words Completed: " + completedWords.size() + "/" + wordsToComplete);
        wordsLabel.setFont(Font.font("Arial", 16));

        Label timeLabel = new Label("Time: " + result.getTimeElapsedSeconds() + " seconds");
        timeLabel.setFont(Font.font("Arial", 14));

        // List completed words
        if (!completedWords.isEmpty()) {
            Label wordsListLabel = new Label("Words: " + String.join(", ", completedWords));
            wordsListLabel.setFont(Font.font("Arial", 12));
            wordsListLabel.setWrapText(true);
            scoreCard.getChildren().addAll(finalScore, wordsLabel, timeLabel, wordsListLabel);
        } else {
            scoreCard.getChildren().addAll(finalScore, wordsLabel, timeLabel);
        }

        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        closeButton.setStyle("-fx-background-color: white; -fx-text-fill: #663399; -fx-background-radius: 15; -fx-padding: 8 30;");
        closeButton.setOnAction(e -> stage.close());

        resultsBox.getChildren().addAll(emojiLabel, titleLabel, scoreCard, closeButton);

        // Keep the same window size
        int windowWidth = (BOARD_WIDTH * CELL_SIZE) + 220;
        int windowHeight = (BOARD_HEIGHT * CELL_SIZE) + 120;
        Scene resultsScene = new Scene(resultsBox, windowWidth, windowHeight);
        stage.setScene(resultsScene);
    }

    /**
     * Inner class representing a falling letter block
     */
    private static class FallingLetter {
        char letter;
        int row;
        int col;
        Color color;

        FallingLetter(char letter, int row, int col, Color color) {
            this.letter = letter;
            this.row = row;
            this.col = col;
            this.color = color;
        }
    }
}
