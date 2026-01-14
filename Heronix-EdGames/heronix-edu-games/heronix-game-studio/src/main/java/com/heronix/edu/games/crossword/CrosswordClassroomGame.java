package com.heronix.edu.games.crossword;

import com.heronix.edu.common.game.*;
import com.heronix.edu.games.crossword.data.WordListLoader;
import com.heronix.edu.games.crossword.generator.CrosswordGenerator;
import com.heronix.edu.games.crossword.model.*;
import com.heronix.edu.games.crossword.scoring.CrosswordScorer;
import com.heronix.edu.games.crossword.ui.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Map;

/**
 * Crossword Classroom - Educational crossword puzzle game.
 * Features subject-based word lists, auto-generated puzzles,
 * and a friendly owl mascot that provides feedback.
 */
public class CrosswordClassroomGame extends AbstractGame {

    private static final String GAME_ID = "crossword-classroom";
    private static final String GAME_NAME = "Crossword Classroom";
    private static final String VERSION = "1.0.0";

    // Game state
    private CrosswordPuzzle puzzle;
    private WordListLoader.Subject selectedSubject;
    private WordListLoader.Difficulty selectedDifficultyLevel;
    private int hintsUsed;
    private int errorsCommitted;
    private int wordsCompleted;

    // UI components
    private Stage stage;
    private BorderPane mainLayout;
    private CrosswordGridView gridView;
    private ClueListView clueListView;
    private OwlMascot owlMascot;
    private Label scoreLabel;
    private Label timeLabel;
    private Label progressLabel;

    // Timer
    private Timeline gameTimer;
    private int elapsedSeconds;

    // Services
    private final CrosswordGenerator generator;
    private CrosswordScorer scorer;

    public CrosswordClassroomGame() {
        super();
        this.generator = new CrosswordGenerator();
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
        return "Challenge yourself with educational crossword puzzles! Learn vocabulary across multiple subjects.";
    }

    @Override
    public GameSubject getSubject() {
        return GameSubject.GENERAL;
    }

    @Override
    public GradeLevel[] getTargetGrades() {
        return new GradeLevel[]{
            GradeLevel.KINDERGARTEN,
            GradeLevel.FIRST, GradeLevel.SECOND, GradeLevel.THIRD,
            GradeLevel.FOURTH, GradeLevel.FIFTH, GradeLevel.SIXTH,
            GradeLevel.SEVENTH, GradeLevel.EIGHTH,
            GradeLevel.NINTH, GradeLevel.TENTH, GradeLevel.ELEVENTH, GradeLevel.TWELFTH
        };
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public DifficultyLevel[] getDifficultyLevels() {
        return new DifficultyLevel[]{
            DifficultyLevel.EASY,
            DifficultyLevel.MEDIUM,
            DifficultyLevel.HARD,
            DifficultyLevel.EXPERT
        };
    }

    @Override
    protected void onInitialize() {
        hintsUsed = 0;
        errorsCommitted = 0;
        wordsCompleted = 0;
        elapsedSeconds = 0;

        // Create the main stage
        Platform.runLater(this::createMainStage);
    }

    private void createMainStage() {
        stage = new Stage();
        stage.setTitle(GAME_NAME);
        stage.initModality(Modality.APPLICATION_MODAL);

        // Show subject selection dialog
        showSubjectSelection();
    }

    private void showSubjectSelection() {
        VBox selectionBox = new VBox(20);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(40));
        selectionBox.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");

        // Title
        Label title = new Label("Crossword Classroom");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        // Owl mascot
        Label owl = new Label("🦉");
        owl.setFont(Font.font("Segoe UI Emoji", 64));

        Label subtitle = new Label("Choose a Subject");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitle.setTextFill(Color.WHITE);

        // Subject buttons
        VBox subjectBox = new VBox(10);
        subjectBox.setAlignment(Pos.CENTER);

        for (WordListLoader.Subject subject : WordListLoader.Subject.values()) {
            Button btn = createSubjectButton(subject);
            subjectBox.getChildren().add(btn);
        }

        // Difficulty selection
        Label diffLabel = new Label("Select Difficulty");
        diffLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        diffLabel.setTextFill(Color.WHITE);

        HBox diffBox = new HBox(10);
        diffBox.setAlignment(Pos.CENTER);

        ToggleGroup diffGroup = new ToggleGroup();
        String[] diffNames = {"Easy (K-2)", "Medium (3-5)", "Hard (6-8)", "Expert (9-12)"};
        WordListLoader.Difficulty[] diffs = WordListLoader.Difficulty.values();

        for (int i = 0; i < diffs.length; i++) {
            RadioButton rb = new RadioButton(diffNames[i]);
            rb.setToggleGroup(diffGroup);
            rb.setUserData(diffs[i]);
            rb.setTextFill(Color.WHITE);
            if (i == 1) rb.setSelected(true); // Default to Medium
            diffBox.getChildren().add(rb);
        }

        selectedDifficultyLevel = WordListLoader.Difficulty.INTERMEDIATE;
        diffGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDifficultyLevel = (WordListLoader.Difficulty) newVal.getUserData();
            }
        });

        selectionBox.getChildren().addAll(owl, title, subtitle, subjectBox, diffLabel, diffBox);

        Scene scene = new Scene(selectionBox, 500, 600);
        stage.setScene(scene);
        stage.show();
    }

    private Button createSubjectButton(WordListLoader.Subject subject) {
        String emoji = getSubjectEmoji(subject);
        Button btn = new Button(emoji + "  " + formatSubjectName(subject));
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 25; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-cursor: hand;"));

        btn.setOnAction(e -> {
            selectedSubject = subject;
            startGame();
        });

        return btn;
    }

    private String getSubjectEmoji(WordListLoader.Subject subject) {
        switch (subject) {
            case MATH: return "🔢";
            case SCIENCE: return "🔬";
            case LANGUAGE_ARTS: return "📚";
            case SOCIAL_STUDIES: return "🌍";
            case HISTORY: return "🏛️";
            default: return "📖";
        }
    }

    private String formatSubjectName(WordListLoader.Subject subject) {
        String name = subject.name().replace("_", " ");
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private void startGame() {
        // Map loader difficulty to game difficulty
        DifficultyLevel gameDifficulty;
        switch (selectedDifficultyLevel) {
            case ELEMENTARY: gameDifficulty = DifficultyLevel.EASY; break;
            case ADVANCED: gameDifficulty = DifficultyLevel.HARD; break;
            default: gameDifficulty = DifficultyLevel.MEDIUM;
        }
        setDifficulty(gameDifficulty);

        // Load word list and generate puzzle
        try {
            WordList wordList = WordListLoader.getInstance().loadForDifficulty(selectedSubject, selectedDifficultyLevel);
            puzzle = generator.generate(wordList, gameDifficulty);
            scorer = new CrosswordScorer(puzzle, gameDifficulty);

            // Build game UI
            buildGameUI();
            start();
        } catch (Exception e) {
            showError("Failed to load game: " + e.getMessage());
        }
    }

    private void buildGameUI() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f4f8;");

        // Top bar
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

        // Center - Grid
        gridView = new CrosswordGridView(puzzle);
        gridView.setOnWordComplete(this::onWordComplete);
        gridView.setOnCellChange(this::onCellChange);
        gridView.setOnClueSelect(this::onClueSelect);

        ScrollPane gridScroll = new ScrollPane(gridView);
        gridScroll.setFitToWidth(true);
        gridScroll.setFitToHeight(true);
        gridScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        mainLayout.setCenter(gridScroll);

        // Right - Clues
        clueListView = new ClueListView();
        clueListView.setClues(puzzle.getAcrossClues(), puzzle.getDownClues());
        clueListView.setOnClueClick(word -> gridView.navigateToWord(word));
        mainLayout.setRight(clueListView);

        // Bottom - Owl mascot and buttons
        HBox bottomBar = createBottomBar();
        mainLayout.setBottom(bottomBar);

        Scene gameScene = new Scene(mainLayout, 1000, 700);
        stage.setScene(gameScene);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #667eea;");

        // Title
        Label title = new Label("🦉 " + GAME_NAME);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        // Subject indicator
        String subjectText = getSubjectEmoji(selectedSubject) + " " + formatSubjectName(selectedSubject);
        Label subjectLabel = new Label(subjectText);
        subjectLabel.setFont(Font.font("Arial", 14));
        subjectLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Score
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        scoreLabel.setTextFill(Color.WHITE);

        // Time
        timeLabel = new Label("Time: 0:00");
        timeLabel.setFont(Font.font("Arial", 14));
        timeLabel.setTextFill(Color.WHITE);

        // Progress
        progressLabel = new Label("0/" + puzzle.getPlacedWords().size() + " words");
        progressLabel.setFont(Font.font("Arial", 14));
        progressLabel.setTextFill(Color.WHITE);

        topBar.getChildren().addAll(title, subjectLabel, spacer, progressLabel, scoreLabel, timeLabel);
        return topBar;
    }

    private HBox createBottomBar() {
        HBox bottomBar = new HBox(20);
        bottomBar.setPadding(new Insets(15));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");

        // Owl mascot
        owlMascot = OwlMascot.createCompact();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Hint button
        Button hintBtn = new Button("💡 Hint");
        hintBtn.setFont(Font.font("Arial", 14));
        hintBtn.setOnAction(e -> useHint());

        // Check button
        Button checkBtn = new Button("✓ Check");
        checkBtn.setFont(Font.font("Arial", 14));
        checkBtn.setOnAction(e -> checkAnswers());

        // Quit button
        Button quitBtn = new Button("Exit");
        quitBtn.setFont(Font.font("Arial", 14));
        quitBtn.setOnAction(e -> confirmQuit());

        bottomBar.getChildren().addAll(owlMascot, spacer, hintBtn, checkBtn, quitBtn);
        return bottomBar;
    }

    @Override
    protected void onStart() {
        // Start timer
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();

        if (owlMascot != null) {
            owlMascot.showReaction(OwlMascot.Reaction.NEUTRAL, "Let's solve this puzzle!");
        }
    }

    private void updateTimer() {
        elapsedSeconds++;
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        Platform.runLater(() -> {
            if (timeLabel != null) {
                timeLabel.setText(String.format("Time: %d:%02d", minutes, seconds));
            }
        });
    }

    private void onCellChange(int row, int col) {
        CrosswordCell cell = puzzle.getCell(row, col);
        if (cell != null && cell.isFilled()) {
            if (cell.isCorrect()) {
                if (owlMascot != null) owlMascot.onCorrectLetter();
            } else {
                errorsCommitted++;
                if (owlMascot != null) owlMascot.onIncorrectLetter();
            }
        }
        updateScore();
    }

    private void onWordComplete(CrosswordWord word) {
        wordsCompleted++;
        if (owlMascot != null) owlMascot.onWordComplete(word.getWord());

        // Update clue list
        for (CrosswordClue clue : puzzle.getAcrossClues()) {
            if (clue.getWordRef() == word) {
                clueListView.markSolved(clue);
                break;
            }
        }
        for (CrosswordClue clue : puzzle.getDownClues()) {
            if (clue.getWordRef() == word) {
                clueListView.markSolved(clue);
                break;
            }
        }

        updateProgress();
        updateScore();

        // Check if puzzle is complete
        if (puzzle.isComplete()) {
            onPuzzleComplete();
        }
    }

    private void onClueSelect(CrosswordClue clue) {
        if (clueListView != null) clueListView.selectClue(clue);
    }

    private void useHint() {
        if (gridView == null) return;
        CrosswordCellView selectedCell = gridView.getSelectedCell();
        if (selectedCell != null && !selectedCell.getCell().isLocked()) {
            puzzle.revealCell(selectedCell.getRow(), selectedCell.getCol());
            selectedCell.updateView();
            hintsUsed++;
            if (owlMascot != null) owlMascot.onHintUsed();
            updateScore();

            // Check if word is now complete
            CrosswordWord word = puzzle.getWordAt(selectedCell.getRow(), selectedCell.getCol(), gridView.getCurrentDirection());
            if (word != null && puzzle.isWordComplete(word)) {
                puzzle.lockWordIfComplete(word);
                onWordComplete(word);
            }
        } else {
            if (owlMascot != null) owlMascot.showReaction(OwlMascot.Reaction.THINKING, "Select a cell first!");
        }
    }

    private void checkAnswers() {
        if (puzzle == null || gridView == null) return;
        int incorrectCount = 0;
        for (int r = 0; r < puzzle.getGridHeight(); r++) {
            for (int c = 0; c < puzzle.getGridWidth(); c++) {
                CrosswordCell cell = puzzle.getCell(r, c);
                if (cell != null && !cell.isBlocked() && cell.isFilled() && !cell.isCorrect()) {
                    gridView.showCellError(r, c);
                    incorrectCount++;
                }
            }
        }

        if (owlMascot != null) {
            if (incorrectCount == 0) {
                owlMascot.showReaction(OwlMascot.Reaction.HAPPY, "Looking good so far!");
            } else {
                owlMascot.showReaction(OwlMascot.Reaction.THINKING, incorrectCount + " incorrect letters found.");
            }
        }
    }

    private void updateScore() {
        if (scorer == null) return;
        int score = scorer.calculateCurrentScore(hintsUsed, errorsCommitted, elapsedSeconds);
        result.setScore(score);
        Platform.runLater(() -> {
            if (scoreLabel != null) scoreLabel.setText("Score: " + score);
        });
    }

    private void updateProgress() {
        if (puzzle == null) return;
        int total = puzzle.getPlacedWords().size();
        Platform.runLater(() -> {
            if (progressLabel != null) progressLabel.setText(wordsCompleted + "/" + total + " words");
        });
    }

    private void onPuzzleComplete() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        // Calculate final score
        if (scorer != null) {
            int finalScore = scorer.calculateFinalScore(hintsUsed, errorsCommitted, elapsedSeconds, true);
            result.setScore(finalScore);
        }
        result.setCompleted(true);
        result.addCustomMetric("hintsUsed", hintsUsed);
        result.addCustomMetric("errorsCommitted", errorsCommitted);
        result.addCustomMetric("wordsCompleted", wordsCompleted);
        if (selectedSubject != null) {
            result.addCustomMetric("subject", selectedSubject.name());
        }

        if (owlMascot != null) owlMascot.onPuzzleComplete();

        // Show completion dialog after a short delay
        Platform.runLater(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            showCompletionDialog();
        });
    }

    private void showCompletionDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Puzzle Complete!");
        alert.setHeaderText("🦉🎉 Congratulations!");
        alert.setContentText(String.format(
            "You completed the puzzle!\n\n" +
            "Score: %d\n" +
            "Time: %d:%02d\n" +
            "Words: %d\n" +
            "Hints Used: %d\n" +
            "Errors: %d",
            result.getScore(),
            elapsedSeconds / 60, elapsedSeconds % 60,
            wordsCompleted,
            hintsUsed,
            errorsCommitted
        ));

        alert.showAndWait();
        stop();
    }

    private void confirmQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Your progress will not be saved.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stop();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Game Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected void onPause() {
        if (gameTimer != null) {
            gameTimer.pause();
        }
    }

    @Override
    protected void onResume() {
        if (gameTimer != null) {
            gameTimer.play();
        }
    }

    @Override
    protected void onStop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }

    @Override
    protected void saveStateData(GameState state) {
        state.putData("hintsUsed", hintsUsed);
        state.putData("errorsCommitted", errorsCommitted);
        state.putData("wordsCompleted", wordsCompleted);
        state.putData("elapsedSeconds", elapsedSeconds);
        if (selectedSubject != null) {
            state.putData("subject", selectedSubject.name());
        }
        if (selectedDifficultyLevel != null) {
            state.putData("difficultyLevel", selectedDifficultyLevel.name());
        }
    }

    @Override
    protected void loadStateData(GameState state) {
        Map<String, Object> data = state.getStateData();
        if (data != null) {
            hintsUsed = data.containsKey("hintsUsed") ? (int) data.get("hintsUsed") : 0;
            errorsCommitted = data.containsKey("errorsCommitted") ? (int) data.get("errorsCommitted") : 0;
            wordsCompleted = data.containsKey("wordsCompleted") ? (int) data.get("wordsCompleted") : 0;
            elapsedSeconds = data.containsKey("elapsedSeconds") ? (int) data.get("elapsedSeconds") : 0;

            String subjectName = (String) data.get("subject");
            if (subjectName != null) {
                selectedSubject = WordListLoader.Subject.valueOf(subjectName);
            }

            String diffName = (String) data.get("difficultyLevel");
            if (diffName != null) {
                selectedDifficultyLevel = WordListLoader.Difficulty.valueOf(diffName);
            }
        }
    }
}
