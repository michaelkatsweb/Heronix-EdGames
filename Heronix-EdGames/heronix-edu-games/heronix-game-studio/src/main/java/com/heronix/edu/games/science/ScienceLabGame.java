package com.heronix.edu.games.science;

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
 * Science Lab - Virtual experiment and science quiz game
 * Students learn scientific concepts through interactive questions
 */
public class ScienceLabGame extends AbstractGame {

    private static final String GAME_ID = "science-lab";
    private static final String GAME_NAME = "Science Lab";
    private static final String VERSION = "1.0.0";

    // Science questions with multiple choice answers
    private static final Question[] QUESTIONS = {
        new Question("What is the chemical symbol for water?", new String[]{"H2O", "CO2", "O2", "NaCl"}, 0, "Chemistry"),
        new Question("What planet is known as the Red Planet?", new String[]{"Venus", "Mars", "Jupiter", "Saturn"}, 1, "Astronomy"),
        new Question("What is the largest organ in the human body?", new String[]{"Heart", "Brain", "Liver", "Skin"}, 3, "Biology"),
        new Question("What force keeps us on the ground?", new String[]{"Magnetism", "Friction", "Gravity", "Inertia"}, 2, "Physics"),
        new Question("What gas do plants absorb from the air?", new String[]{"Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"}, 2, "Biology"),
        new Question("What is the freezing point of water in Celsius?", new String[]{"0°C", "32°C", "100°C", "-10°C"}, 0, "Chemistry"),
        new Question("Which planet has the most moons?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 3, "Astronomy"),
        new Question("What type of energy does the sun provide?", new String[]{"Nuclear", "Solar", "Wind", "Hydro"}, 1, "Physics"),
        new Question("What is the hardest natural substance?", new String[]{"Gold", "Iron", "Diamond", "Quartz"}, 2, "Chemistry"),
        new Question("How many bones are in the adult human body?", new String[]{"106", "206", "306", "406"}, 1, "Biology"),
        new Question("What is the speed of light?", new String[]{"300 km/s", "300,000 km/s", "3,000 km/s", "30,000 km/s"}, 1, "Physics"),
        new Question("Which gas makes up most of Earth's atmosphere?", new String[]{"Oxygen", "Carbon Dioxide", "Nitrogen", "Argon"}, 2, "Chemistry"),
        new Question("What is the powerhouse of the cell?", new String[]{"Nucleus", "Ribosome", "Mitochondria", "Chloroplast"}, 2, "Biology"),
        new Question("What causes tides on Earth?", new String[]{"Wind", "Sun", "Moon's gravity", "Earth's rotation"}, 2, "Astronomy"),
        new Question("What is the SI unit of force?", new String[]{"Watt", "Joule", "Newton", "Pascal"}, 2, "Physics")
    };

    private List<Question> questionList;
    private int currentQuestionIndex;
    private Question currentQuestion;
    private Random random;

    // UI Components
    private Stage stage;
    private Label questionLabel;
    private Label categoryLabel;
    private Label scoreLabel;
    private Label progressLabel;
    private Label feedbackLabel;
    private Button[] answerButtons;
    private AnimationTimer timer;

    public ScienceLabGame() {
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
        return "Conduct virtual experiments and explore scientific concepts!";
    }

    @Override
    public GameSubject getSubject() {
        return GameSubject.SCIENCE;
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

        // Shuffle and select questions
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
        stage.setTitle("Science Lab - Experiment & Learn!");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Gradient background (cool blue/teal)
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(0, 150, 136)),
            new Stop(0.5, Color.rgb(0, 121, 107)),
            new Stop(1, Color.rgb(0, 96, 100))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title
        Label titleLabel = new Label("SCIENCE LAB");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        // Stats bar
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreLabel.setTextFill(Color.WHITE);

        progressLabel = new Label("Question: 1/" + questionList.size());
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        progressLabel.setTextFill(Color.WHITE);

        statsBox.getChildren().addAll(scoreLabel, progressLabel);

        // Question area
        VBox questionArea = new VBox(20);
        questionArea.setAlignment(Pos.CENTER);
        questionArea.setPadding(new Insets(30));
        questionArea.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20;");

        Label labEmoji = new Label("🔬");
        labEmoji.setFont(Font.font(50));

        categoryLabel = new Label("Category");
        categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        categoryLabel.setTextFill(Color.rgb(0, 150, 136));
        categoryLabel.setStyle("-fx-background-color: rgba(0,150,136,0.1); -fx-padding: 5 15; -fx-background-radius: 15;");

        questionLabel = new Label("Question goes here?");
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
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
            answerButtons[i].setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10;");
            answerButtons[i].setOnAction(e -> checkAnswer(index));

            // Hover effect
            answerButtons[i].setOnMouseEntered(e -> {
                if (!answerButtons[index].isDisabled()) {
                    answerButtons[index].setStyle("-fx-background-color: #00796B; -fx-text-fill: white; -fx-background-radius: 10;");
                }
            });
            answerButtons[i].setOnMouseExited(e -> {
                if (!answerButtons[index].isDisabled()) {
                    answerButtons[index].setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10;");
                }
            });

            answersBox.getChildren().add(answerButtons[i]);
        }

        questionArea.getChildren().addAll(labEmoji, categoryLabel, questionLabel, feedbackLabel, answersBox);

        root.getChildren().addAll(titleLabel, statsBox, questionArea);

        Scene scene = new Scene(root, 700, 700);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void showQuestion(Question question) {
        currentQuestion = question;
        categoryLabel.setText(question.category);
        questionLabel.setText(question.text);
        feedbackLabel.setText("");
        progressLabel.setText("Question: " + (currentQuestionIndex + 1) + "/" + questionList.size());

        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(question.answers[i]);
            answerButtons[i].setDisable(false);
            answerButtons[i].setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10;");
        }
    }

    private void checkAnswer(int selectedIndex) {
        // Disable all buttons
        for (Button btn : answerButtons) {
            btn.setDisable(true);
        }

        if (selectedIndex == currentQuestion.correctIndex) {
            result.incrementCorrectAnswers();
            result.setScore(result.getScore() + 10);
            feedbackLabel.setText("Correct! Great scientific thinking!");
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
            new Stop(0, Color.rgb(0, 150, 136)),
            new Stop(1, Color.rgb(0, 96, 100))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        resultsBox.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        double percentage = (result.getScore() * 100.0) / result.getMaxScore();
        String emoji = percentage >= 90 ? "🏆" : percentage >= 70 ? "🔬" : percentage >= 50 ? "🧪" : "📚";

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(80));

        Label titleLabel = new Label("Experiment Complete!");
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

        Button closeButton = new Button("Close Lab");
        closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        closeButton.setStyle("-fx-background-color: white; -fx-text-fill: #009688; -fx-background-radius: 20; -fx-padding: 10 40;");
        closeButton.setOnAction(e -> stage.close());

        resultsBox.getChildren().addAll(emojiLabel, titleLabel, scoreCard, closeButton);

        Scene resultsScene = new Scene(resultsBox, 700, 700);
        stage.setScene(resultsScene);
    }

    /**
     * Inner class for science questions
     */
    private static class Question {
        String text;
        String[] answers;
        int correctIndex;
        String category;

        Question(String text, String[] answers, int correctIndex, String category) {
            this.text = text;
            this.answers = answers;
            this.correctIndex = correctIndex;
            this.category = category;
        }
    }
}
