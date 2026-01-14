package com.heronix.edu.games.history;

import com.heronix.edu.common.game.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
 * History Explorer - Time travel through history quiz game
 * Students learn about historical events, figures, and civilizations
 */
public class HistoryExplorerGame extends AbstractGame {

    private static final String GAME_ID = "history-explorer";
    private static final String GAME_NAME = "History Explorer";
    private static final String VERSION = "1.0.0";

    // History questions with multiple choice answers
    private static final Question[] QUESTIONS = {
        new Question("Who was the first President of the United States?",
            new String[]{"Thomas Jefferson", "George Washington", "Abraham Lincoln", "John Adams"}, 1, "American History"),
        new Question("In what year did World War II end?",
            new String[]{"1943", "1944", "1945", "1946"}, 2, "World Wars"),
        new Question("Which ancient civilization built the pyramids?",
            new String[]{"Romans", "Greeks", "Egyptians", "Mayans"}, 2, "Ancient Civilizations"),
        new Question("Who wrote the Declaration of Independence?",
            new String[]{"George Washington", "Benjamin Franklin", "Thomas Jefferson", "John Hancock"}, 2, "American History"),
        new Question("What was the name of the ship the Pilgrims sailed on?",
            new String[]{"Santa Maria", "Mayflower", "Endeavour", "Titanic"}, 1, "Colonial America"),
        new Question("Which explorer discovered America in 1492?",
            new String[]{"Vasco da Gama", "Ferdinand Magellan", "Christopher Columbus", "Marco Polo"}, 2, "Exploration"),
        new Question("Who was known as the 'Father of Medicine'?",
            new String[]{"Aristotle", "Hippocrates", "Socrates", "Plato"}, 1, "Ancient Greece"),
        new Question("The Great Wall was built by which country?",
            new String[]{"Japan", "India", "China", "Korea"}, 2, "Ancient Civilizations"),
        new Question("Who invented the printing press?",
            new String[]{"Leonardo da Vinci", "Johannes Gutenberg", "Galileo Galilei", "Isaac Newton"}, 1, "Renaissance"),
        new Question("What year did the American Civil War begin?",
            new String[]{"1850", "1861", "1865", "1870"}, 1, "American History"),
        new Question("Who was the Queen of Egypt known for her beauty?",
            new String[]{"Nefertiti", "Cleopatra", "Hatshepsut", "Isis"}, 1, "Ancient Egypt"),
        new Question("Which empire was ruled by Julius Caesar?",
            new String[]{"Greek Empire", "Persian Empire", "Roman Empire", "Byzantine Empire"}, 2, "Ancient Rome"),
        new Question("What was the Renaissance?",
            new String[]{"A war", "A disease", "A cultural rebirth", "A type of food"}, 2, "Renaissance"),
        new Question("Who led India's independence movement?",
            new String[]{"Jawaharlal Nehru", "Mahatma Gandhi", "Subhas Chandra Bose", "Indira Gandhi"}, 1, "Modern History"),
        new Question("The Boston Tea Party protested what?",
            new String[]{"Slavery", "British taxes", "French invasion", "Spanish rule"}, 1, "American Revolution")
    };

    private List<Question> questionList;
    private int currentQuestionIndex;
    private Question currentQuestion;
    private Random random;

    // UI Components
    private Stage stage;
    private Label questionLabel;
    private Label eraLabel;
    private Label scoreLabel;
    private Label progressLabel;
    private Label feedbackLabel;
    private Button[] answerButtons;
    private AnimationTimer timer;

    public HistoryExplorerGame() {
        super();
        this.random = new Random();
        this.questionList = new ArrayList<>();
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
        return "Travel through time and discover historical events!";
    }

    @Override
    public GameSubject getSubject() {
        return GameSubject.SOCIAL_STUDIES;
    }

    @Override
    public GradeLevel[] getTargetGrades() {
        return new GradeLevel[]{
            GradeLevel.FOURTH, GradeLevel.FIFTH,
            GradeLevel.SIXTH, GradeLevel.SEVENTH, GradeLevel.EIGHTH
        };
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    protected void onInitialize() {
        int questionCount;
        switch (context.getDifficultyLevel()) {
            case EASY:
                questionCount = 8;
                break;
            case MEDIUM:
                questionCount = 10;
                break;
            case HARD:
            default:
                questionCount = 12;
                break;
        }

        List<Question> shuffled = new ArrayList<>(Arrays.asList(QUESTIONS));
        Collections.shuffle(shuffled, random);
        questionList = shuffled.subList(0, Math.min(questionCount, shuffled.size()));

        Platform.runLater(this::setupUI);

        result.setMaxScore(questionList.size() * 10);
        result.addCustomMetric("totalQuestions", questionList.size());
    }

    @Override
    protected void onStart() {
        currentQuestionIndex = 0;
        Platform.runLater(() -> {
            showQuestion(questionList.get(currentQuestionIndex));
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
        state.setCurrentQuestion(currentQuestionIndex);
    }

    @Override
    protected void loadStateData(GameState state) {
        currentQuestionIndex = state.getCurrentQuestion();
    }

    private void setupUI() {
        stage = new Stage();
        stage.setTitle("History Explorer - Journey Through Time!");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Gradient background (warm brown/sepia tones)
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(139, 90, 43)),
            new Stop(0.5, Color.rgb(121, 85, 61)),
            new Stop(1, Color.rgb(101, 67, 33))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title
        Label titleLabel = new Label("HISTORY EXPLORER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.rgb(255, 248, 220)); // Cornsilk

        // Stats bar
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreLabel.setTextFill(Color.rgb(255, 248, 220));

        progressLabel = new Label("Question: 1/" + questionList.size());
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        progressLabel.setTextFill(Color.rgb(255, 248, 220));

        statsBox.getChildren().addAll(scoreLabel, progressLabel);

        // Question area
        VBox questionArea = new VBox(20);
        questionArea.setAlignment(Pos.CENTER);
        questionArea.setPadding(new Insets(30));
        questionArea.setStyle("-fx-background-color: rgba(255,248,220,0.95); -fx-background-radius: 20;");

        Label scrollEmoji = new Label("📜");
        scrollEmoji.setFont(Font.font(50));

        eraLabel = new Label("Era");
        eraLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        eraLabel.setTextFill(Color.rgb(139, 90, 43));
        eraLabel.setStyle("-fx-background-color: rgba(139,90,43,0.1); -fx-padding: 5 15; -fx-background-radius: 15;");

        questionLabel = new Label("Question goes here?");
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        questionLabel.setTextFill(Color.rgb(51, 51, 51));
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(500);
        questionLabel.setAlignment(Pos.CENTER);

        feedbackLabel = new Label("");
        feedbackLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        feedbackLabel.setMinHeight(25);

        // Answer buttons
        VBox answersBox = new VBox(10);
        answersBox.setAlignment(Pos.CENTER);
        answerButtons = new Button[4];

        for (int i = 0; i < 4; i++) {
            final int index = i;
            answerButtons[i] = new Button("Answer " + (i + 1));
            answerButtons[i].setFont(Font.font("Arial", 16));
            answerButtons[i].setPrefWidth(350);
            answerButtons[i].setPrefHeight(45);
            answerButtons[i].setStyle("-fx-background-color: #d4c4a8; -fx-background-radius: 10;");
            answerButtons[i].setOnAction(e -> checkAnswer(index));

            answerButtons[i].setOnMouseEntered(e -> {
                if (!answerButtons[index].isDisabled()) {
                    answerButtons[index].setStyle("-fx-background-color: #8B5A2B; -fx-text-fill: white; -fx-background-radius: 10;");
                }
            });
            answerButtons[i].setOnMouseExited(e -> {
                if (!answerButtons[index].isDisabled()) {
                    answerButtons[index].setStyle("-fx-background-color: #d4c4a8; -fx-background-radius: 10;");
                }
            });

            answersBox.getChildren().add(answerButtons[i]);
        }

        questionArea.getChildren().addAll(scrollEmoji, eraLabel, questionLabel, feedbackLabel, answersBox);

        root.getChildren().addAll(titleLabel, statsBox, questionArea);

        Scene scene = new Scene(root, 700, 700);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void showQuestion(Question question) {
        currentQuestion = question;
        eraLabel.setText(question.era);
        questionLabel.setText(question.text);
        feedbackLabel.setText("");
        progressLabel.setText("Question: " + (currentQuestionIndex + 1) + "/" + questionList.size());

        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(question.answers[i]);
            answerButtons[i].setDisable(false);
            answerButtons[i].setStyle("-fx-background-color: #d4c4a8; -fx-background-radius: 10;");
        }
    }

    private void checkAnswer(int selectedIndex) {
        for (Button btn : answerButtons) {
            btn.setDisable(true);
        }

        if (selectedIndex == currentQuestion.correctIndex) {
            result.incrementCorrectAnswers();
            result.setScore(result.getScore() + 10);
            feedbackLabel.setText("Correct! You're a history expert!");
            feedbackLabel.setTextFill(Color.rgb(76, 175, 80));
            answerButtons[selectedIndex].setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10;");
            scoreLabel.setText("Score: " + result.getScore());
        } else {
            result.incrementIncorrectAnswers();
            feedbackLabel.setText("Not quite! The answer was: " + currentQuestion.answers[currentQuestion.correctIndex]);
            feedbackLabel.setTextFill(Color.rgb(244, 67, 54));
            answerButtons[selectedIndex].setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10;");
            answerButtons[currentQuestion.correctIndex].setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10;");
        }

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(this::nextQuestion);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }).start();
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex >= questionList.size()) {
            stop();
        } else {
            showQuestion(questionList.get(currentQuestionIndex));
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
            new Stop(0, Color.rgb(139, 90, 43)),
            new Stop(1, Color.rgb(101, 67, 33))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        resultsBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        double percentage = (result.getScore() * 100.0) / result.getMaxScore();
        String emoji = percentage >= 90 ? "🏆" : percentage >= 70 ? "📜" : percentage >= 50 ? "🏛️" : "📚";

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(80));

        Label titleLabel = new Label("Journey Complete!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.rgb(255, 248, 220));

        VBox scoreCard = new VBox(15);
        scoreCard.setAlignment(Pos.CENTER);
        scoreCard.setPadding(new Insets(25));
        scoreCard.setStyle("-fx-background-color: rgba(255,248,220,0.95); -fx-background-radius: 15;");
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

        Button closeButton = new Button("Return Home");
        closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        closeButton.setStyle("-fx-background-color: #FFF8DC; -fx-text-fill: #8B5A2B; -fx-background-radius: 20; -fx-padding: 10 40;");
        closeButton.setOnAction(e -> stage.close());

        resultsBox.getChildren().addAll(emojiLabel, titleLabel, scoreCard, closeButton);

        Scene resultsScene = new Scene(resultsBox, 700, 700);
        stage.setScene(resultsScene);
    }

    /**
     * Inner class for history questions
     */
    private static class Question {
        String text;
        String[] answers;
        int correctIndex;
        String era;

        Question(String text, String[] answers, int correctIndex, String era) {
            this.text = text;
            this.answers = answers;
            this.correctIndex = correctIndex;
            this.era = era;
        }
    }
}
