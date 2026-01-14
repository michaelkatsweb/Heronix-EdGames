package com.heronix.edu.server.dto.game;

/**
 * Request from player submitting an answer to a question.
 */
public class AnswerRequest {
    private String questionId;
    private String answer;
    private Long answerTimeMs;  // Time taken to answer in milliseconds

    public AnswerRequest() {}

    public AnswerRequest(String questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getAnswerTimeMs() {
        return answerTimeMs;
    }

    public void setAnswerTimeMs(Long answerTimeMs) {
        this.answerTimeMs = answerTimeMs;
    }
}
