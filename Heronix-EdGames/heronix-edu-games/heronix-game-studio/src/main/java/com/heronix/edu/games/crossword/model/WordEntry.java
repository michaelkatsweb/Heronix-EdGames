package com.heronix.edu.games.crossword.model;

/**
 * Represents a word/clue pair from a word list.
 * Used as input for crossword puzzle generation.
 */
public class WordEntry {
    private String word;        // Uppercase, letters only, 3-15 chars
    private String clue;        // Clue text for the word
    private String category;    // Optional sub-category (e.g., "geometry", "addition")
    private int difficulty;     // 1-5 scale within the word list

    public WordEntry() {}

    public WordEntry(String word, String clue) {
        this.word = word.toUpperCase().replaceAll("[^A-Z]", "");
        this.clue = clue;
        this.difficulty = 1;
    }

    public WordEntry(String word, String clue, String category, int difficulty) {
        this.word = word.toUpperCase().replaceAll("[^A-Z]", "");
        this.clue = clue;
        this.category = category;
        this.difficulty = Math.max(1, Math.min(5, difficulty));
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word != null ? word.toUpperCase().replaceAll("[^A-Z]", "") : null;
    }

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
    }

    public int getLength() {
        return word != null ? word.length() : 0;
    }

    @Override
    public String toString() {
        return "WordEntry{word='" + word + "', clue='" + clue + "', difficulty=" + difficulty + "}";
    }
}
