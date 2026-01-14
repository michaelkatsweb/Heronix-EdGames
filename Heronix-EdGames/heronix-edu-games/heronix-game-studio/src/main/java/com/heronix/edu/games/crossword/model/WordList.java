package com.heronix.edu.games.crossword.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of word entries for a specific subject and grade level.
 * Loaded from JSON resources bundled with the game.
 */
public class WordList {
    private String subject;         // MATH, SCIENCE, LANGUAGE_ARTS, SOCIAL_STUDIES, HISTORY
    private String gradeRange;      // "K-2", "3-5", "6-8", "9-12"
    private String name;            // Display name (e.g., "Elementary Math Terms")
    private String description;     // Optional description
    private List<WordEntry> entries;

    public WordList() {
        this.entries = new ArrayList<>();
    }

    public WordList(String subject, String gradeRange, String name) {
        this.subject = subject;
        this.gradeRange = gradeRange;
        this.name = name;
        this.entries = new ArrayList<>();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGradeRange() {
        return gradeRange;
    }

    public void setGradeRange(String gradeRange) {
        this.gradeRange = gradeRange;
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

    public List<WordEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<WordEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
    }

    public void addEntry(WordEntry entry) {
        if (this.entries == null) {
            this.entries = new ArrayList<>();
        }
        this.entries.add(entry);
    }

    public int getWordCount() {
        return entries != null ? entries.size() : 0;
    }

    /**
     * Get words filtered by maximum length.
     */
    public List<WordEntry> getWordsWithMaxLength(int maxLength) {
        return entries.stream()
                .filter(e -> e.getLength() <= maxLength)
                .toList();
    }

    /**
     * Get words filtered by length range.
     */
    public List<WordEntry> getWordsInLengthRange(int minLength, int maxLength) {
        return entries.stream()
                .filter(e -> e.getLength() >= minLength && e.getLength() <= maxLength)
                .toList();
    }

    /**
     * Get words filtered by difficulty.
     */
    public List<WordEntry> getWordsByDifficulty(int maxDifficulty) {
        return entries.stream()
                .filter(e -> e.getDifficulty() <= maxDifficulty)
                .toList();
    }

    @Override
    public String toString() {
        return "WordList{subject='" + subject + "', gradeRange='" + gradeRange +
               "', name='" + name + "', wordCount=" + getWordCount() + "}";
    }
}
