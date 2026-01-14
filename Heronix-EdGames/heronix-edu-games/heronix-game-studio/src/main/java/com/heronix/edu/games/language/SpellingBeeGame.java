package com.heronix.edu.games.language;

import com.heronix.edu.common.game.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

/**
 * Spelling Bee - Word spelling practice game
 * Students hear words and type them correctly
 */
public class SpellingBeeGame extends AbstractGame {

    private static final String GAME_ID = "spelling-bee";
    private static final String GAME_NAME = "Spelling Bee";
    private static final String VERSION = "1.0.0";

    // Word lists by difficulty
    private static final String[] EASY_WORDS = {
        "cat", "dog", "sun", "run", "big", "red", "hat", "cup", "bed", "pen",
        "fish", "bird", "tree", "book", "hand", "food", "door", "ball", "home", "play"
    };

    private static final String[] MEDIUM_WORDS = {
        "happy", "water", "animal", "friend", "school", "garden", "yellow", "family",
        "people", "children", "weather", "kitchen", "picture", "another", "country"
    };

    private static final String[] HARD_WORDS = {
        "beautiful", "different", "important", "education", "vocabulary",
        "adventure", "celebrate", "challenge", "paragraph", "knowledge",
        "necessary", "responsible", "environment", "entertainment", "independent"
    };

    private List<String> wordList;
    private int currentWordIndex;
    private String currentWord;
    private Random random;

    // UI Components
    private Stage stage;
    private Label wordDisplayLabel;
    private Label hintLabel;
    private Label scoreLabel;
    private Label progressLabel;
    private Label feedbackLabel;
    private TextField answerField;
    private Button submitButton;
    private Button hintButton;
    private AnimationTimer timer;

    public SpellingBeeGame() {
        super();
        this.random = new Random();
        this.wordList = new ArrayList<>();
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
        return "Master spelling through fun word challenges!";
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
        // Select word list based on difficulty
        String[] sourceWords;
        int wordCount;

        switch (context.getDifficultyLevel()) {
            case EASY:
                sourceWords = EASY_WORDS;
                wordCount = 10;
                break;
            case MEDIUM:
                sourceWords = MEDIUM_WORDS;
                wordCount = 12;
                break;
            case HARD:
            default:
                sourceWords = HARD_WORDS;
                wordCount = 15;
                break;
        }

        // Shuffle and select words
        List<String> shuffled = new ArrayList<>(Arrays.asList(sourceWords));
        Collections.shuffle(shuffled, random);
        wordList = shuffled.subList(0, Math.min(wordCount, shuffled.size()));

        Platform.runLater(this::setupUI);

        result.setMaxScore(wordList.size() * 10);
        result.addCustomMetric("totalWords", wordList.size());
    }

    @Override
    protected void onStart() {
        currentWordIndex = 0;
        Platform.runLater(() -> {
            showWord(wordList.get(currentWordIndex));
            startTimer();
        });
    }

    @Override
    protected void onPause() {
        if (timer != null) timer.stop();
    }

    @Override
    protected void onResume() {
        if (timer != null) timer.start();
    }

    @Override
    protected void onStop() {
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
        stage.setTitle("Spelling Bee - Word Challenge!");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Gradient background (warm yellow/orange)
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(255, 200, 87)),
            new Stop(0.5, Color.rgb(255, 165, 0)),
            new Stop(1, Color.rgb(255, 140, 0))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title
        Label titleLabel = new Label("SPELLING BEE");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        // Stats bar
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreLabel.setTextFill(Color.WHITE);

        progressLabel = new Label("Word: 1/" + wordList.size());
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        progressLabel.setTextFill(Color.WHITE);

        statsBox.getChildren().addAll(scoreLabel, progressLabel);

        // Word display area
        VBox wordArea = new VBox(15);
        wordArea.setAlignment(Pos.CENTER);
        wordArea.setPadding(new Insets(30));
        wordArea.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20;");

        Label beeEmoji = new Label("🐝");
        beeEmoji.setFont(Font.font(60));

        wordDisplayLabel = new Label("_ _ _ _");
        wordDisplayLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 42));
        wordDisplayLabel.setTextFill(Color.rgb(51, 51, 51));

        hintLabel = new Label("");
        hintLabel.setFont(Font.font("Arial", 16));
        hintLabel.setTextFill(Color.rgb(100, 100, 100));

        answerField = new TextField();
        answerField.setMaxWidth(300);
        answerField.setFont(Font.font("Arial", 24));
        answerField.setAlignment(Pos.CENTER);
        answerField.setPromptText("Type the word...");
        answerField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FFA500; -fx-border-width: 2;");
        answerField.setOnAction(e -> checkAnswer());

        feedbackLabel = new Label("");
        feedbackLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        feedbackLabel.setMinHeight(30);

        wordArea.getChildren().addAll(beeEmoji, wordDisplayLabel, hintLabel, answerField, feedbackLabel);

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        hintButton = new Button("Show Hint");
        hintButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        hintButton.setStyle("-fx-background-color: white; -fx-text-fill: #FFA500; -fx-background-radius: 20; -fx-padding: 10 25;");
        hintButton.setOnAction(e -> showHint());

        submitButton = new Button("Check Spelling");
        submitButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 25;");
        submitButton.setOnAction(e -> checkAnswer());

        buttonBox.getChildren().addAll(hintButton, submitButton);

        root.getChildren().addAll(titleLabel, statsBox, wordArea, buttonBox);

        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        answerField.requestFocus();
    }

    private void showWord(String word) {
        currentWord = word;
        // Show blanks for letters
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            blanks.append("_ ");
        }
        wordDisplayLabel.setText(blanks.toString().trim());
        hintLabel.setText("(" + word.length() + " letters)");
        answerField.clear();
        feedbackLabel.setText("");
        progressLabel.setText("Word: " + (currentWordIndex + 1) + "/" + wordList.size());
    }

    private void showHint() {
        // Show first and last letter
        if (currentWord.length() >= 2) {
            StringBuilder hint = new StringBuilder();
            hint.append(currentWord.charAt(0)).append(" ");
            for (int i = 1; i < currentWord.length() - 1; i++) {
                hint.append("_ ");
            }
            hint.append(currentWord.charAt(currentWord.length() - 1));
            wordDisplayLabel.setText(hint.toString());
            hintLabel.setText("Hint: First and last letters shown (-2 points)");
            // Deduct points for using hint
            result.addCustomMetric("hintsUsed",
                (Integer) result.getCustomMetrics().getOrDefault("hintsUsed", 0) + 1);
        }
    }

    private void checkAnswer() {
        String answer = answerField.getText().trim().toLowerCase();
        if (answer.isEmpty()) return;

        if (answer.equals(currentWord.toLowerCase())) {
            result.incrementCorrectAnswers();
            result.setScore(result.getScore() + 10);
            feedbackLabel.setText("Correct! Great spelling!");
            feedbackLabel.setTextFill(Color.rgb(76, 175, 80));
            scoreLabel.setText("Score: " + result.getScore());
        } else {
            result.incrementIncorrectAnswers();
            feedbackLabel.setText("Oops! It was: " + currentWord);
            feedbackLabel.setTextFill(Color.rgb(244, 67, 54));
        }

        answerField.setDisable(true);
        submitButton.setDisable(true);
        hintButton.setDisable(true);

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(() -> {
                    answerField.setDisable(false);
                    submitButton.setDisable(false);
                    hintButton.setDisable(false);
                    nextWord();
                });
            } catch (InterruptedException ex) {
                // Ignore
            }
        }).start();
    }

    private void nextWord() {
        currentWordIndex++;
        if (currentWordIndex >= wordList.size()) {
            stop();
        } else {
            showWord(wordList.get(currentWordIndex));
            answerField.requestFocus();
        }
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
        VBox resultsBox = new VBox(20);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setPadding(new Insets(40));

        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(255, 200, 87)),
            new Stop(1, Color.rgb(255, 140, 0))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        resultsBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        double percentage = (result.getScore() * 100.0) / result.getMaxScore();
        String emoji = percentage >= 90 ? "🏆" : percentage >= 70 ? "🐝" : percentage >= 50 ? "⭐" : "📖";

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(80));

        Label titleLabel = new Label("Spelling Complete!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        VBox scoreCard = new VBox(15);
        scoreCard.setAlignment(Pos.CENTER);
        scoreCard.setPadding(new Insets(25));
        scoreCard.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15;");
        scoreCard.setMaxWidth(400);

        Label finalScore = new Label("Score: " + result.getScore() + "/" + result.getMaxScore());
        finalScore.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        finalScore.setTextFill(percentage >= 70 ? Color.rgb(76, 175, 80) : Color.rgb(244, 67, 54));

        Label accuracyLabel = new Label(String.format("Accuracy: %.0f%%", result.getAccuracy()));
        accuracyLabel.setFont(Font.font("Arial", 22));

        Label detailsLabel = new Label(
            "Correct: " + result.getCorrectAnswers() +
            "  |  Wrong: " + result.getIncorrectAnswers() +
            "  |  Time: " + result.getTimeElapsedSeconds() + "s"
        );
        detailsLabel.setFont(Font.font("Arial", 14));

        scoreCard.getChildren().addAll(finalScore, accuracyLabel, detailsLabel);

        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        closeButton.setStyle("-fx-background-color: white; -fx-text-fill: #FFA500; -fx-background-radius: 20; -fx-padding: 10 40;");
        closeButton.setOnAction(e -> stage.close());

        resultsBox.getChildren().addAll(emojiLabel, titleLabel, scoreCard, closeButton);

        Scene resultsScene = new Scene(resultsBox, 700, 600);
        stage.setScene(resultsScene);
    }
}
