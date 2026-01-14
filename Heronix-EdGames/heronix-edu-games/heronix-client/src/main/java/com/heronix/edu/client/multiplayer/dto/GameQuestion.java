package com.heronix.edu.client.multiplayer.dto;

import java.util.List;

/**
 * Represents a question sent to the player in Code Breaker game.
 */
public class GameQuestion {
    private String questionId;
    private String questionText;
    private List<String> options;
    private int timeLimit; // seconds

    public GameQuestion() {}

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
}
