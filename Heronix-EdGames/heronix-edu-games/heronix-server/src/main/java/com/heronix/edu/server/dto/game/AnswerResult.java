package com.heronix.edu.server.dto.game;

import java.util.List;

/**
 * Response after submitting an answer.
 * If correct, includes reward options to choose from.
 */
public class AnswerResult {
    private boolean correct;
    private String correctAnswer;  // Shown if incorrect
    private String explanation;    // Optional explanation
    private List<RewardOption> rewardOptions;  // Available if correct
    private Integer creditsEarned;  // If auto-reward
    private Integer newTotalCredits;

    public AnswerResult() {}

    public static AnswerResult correct(List<RewardOption> options) {
        AnswerResult result = new AnswerResult();
        result.correct = true;
        result.rewardOptions = options;
        return result;
    }

    public static AnswerResult incorrect(String correctAnswer, String explanation) {
        AnswerResult result = new AnswerResult();
        result.correct = false;
        result.correctAnswer = correctAnswer;
        result.explanation = explanation;
        return result;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<RewardOption> getRewardOptions() {
        return rewardOptions;
    }

    public void setRewardOptions(List<RewardOption> rewardOptions) {
        this.rewardOptions = rewardOptions;
    }

    public Integer getCreditsEarned() {
        return creditsEarned;
    }

    public void setCreditsEarned(Integer creditsEarned) {
        this.creditsEarned = creditsEarned;
    }

    public Integer getNewTotalCredits() {
        return newTotalCredits;
    }

    public void setNewTotalCredits(Integer newTotalCredits) {
        this.newTotalCredits = newTotalCredits;
    }
}
