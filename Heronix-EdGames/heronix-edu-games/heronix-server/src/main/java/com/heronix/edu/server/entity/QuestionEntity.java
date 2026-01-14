package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a single question in a question set.
 * Used for multiplayer game modes like Code Breaker.
 */
@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @Column(name = "question_id", length = 50)
    private String questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private QuestionSetEntity questionSet;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(name = "wrong_answer_1", length = 500)
    private String wrongAnswer1;

    @Column(name = "wrong_answer_2", length = 500)
    private String wrongAnswer2;

    @Column(name = "wrong_answer_3", length = 500)
    private String wrongAnswer3;

    @Column(name = "difficulty")
    private Integer difficulty = 1;  // 1-5 scale

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "explanation", length = 1000)
    private String explanation;  // Shown after answering

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public QuestionEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public QuestionEntity(String questionId, String questionText, String correctAnswer) {
        this();
        this.questionId = questionId;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Get all answer options (correct + wrong) shuffled.
     * Returns array with correct answer at random position.
     */
    public String[] getAllAnswers() {
        java.util.List<String> answers = new java.util.ArrayList<>();
        answers.add(correctAnswer);
        if (wrongAnswer1 != null && !wrongAnswer1.isBlank()) answers.add(wrongAnswer1);
        if (wrongAnswer2 != null && !wrongAnswer2.isBlank()) answers.add(wrongAnswer2);
        if (wrongAnswer3 != null && !wrongAnswer3.isBlank()) answers.add(wrongAnswer3);
        java.util.Collections.shuffle(answers);
        return answers.toArray(new String[0]);
    }

    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public QuestionSetEntity getQuestionSet() {
        return questionSet;
    }

    public void setQuestionSet(QuestionSetEntity questionSet) {
        this.questionSet = questionSet;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getWrongAnswer1() {
        return wrongAnswer1;
    }

    public void setWrongAnswer1(String wrongAnswer1) {
        this.wrongAnswer1 = wrongAnswer1;
    }

    public String getWrongAnswer2() {
        return wrongAnswer2;
    }

    public void setWrongAnswer2(String wrongAnswer2) {
        this.wrongAnswer2 = wrongAnswer2;
    }

    public String getWrongAnswer3() {
        return wrongAnswer3;
    }

    public void setWrongAnswer3(String wrongAnswer3) {
        this.wrongAnswer3 = wrongAnswer3;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
