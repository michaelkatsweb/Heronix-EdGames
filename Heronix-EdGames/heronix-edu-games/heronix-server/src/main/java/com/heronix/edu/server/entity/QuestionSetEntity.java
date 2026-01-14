package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a set of questions for multiplayer games.
 * Created and managed by teachers.
 */
@Entity
@Table(name = "question_sets")
public class QuestionSetEntity {

    @Id
    @Column(name = "set_id", length = 50)
    private String setId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "subject")
    private String subject;  // MATH, SCIENCE, READING, etc.

    @Column(name = "grade_level")
    private String gradeLevel;  // K-2, 3-5, 6-8, etc.

    @Column(name = "created_by", nullable = false)
    private String createdBy;  // teacherId

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<QuestionEntity> questions = new ArrayList<>();

    // Constructors
    public QuestionSetEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public QuestionSetEntity(String setId, String name, String createdBy) {
        this();
        this.setId = setId;
        this.name = name;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<QuestionEntity> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionEntity> questions) {
        this.questions = questions;
    }

    public void addQuestion(QuestionEntity question) {
        questions.add(question);
        question.setQuestionSet(this);
    }

    public void removeQuestion(QuestionEntity question) {
        questions.remove(question);
        question.setQuestionSet(null);
    }

    public int getQuestionCount() {
        return questions.size();
    }
}
