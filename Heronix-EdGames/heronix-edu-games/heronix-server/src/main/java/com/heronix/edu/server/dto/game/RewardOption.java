package com.heronix.edu.server.dto.game;

/**
 * Represents a reward option that a player can choose after answering correctly.
 */
public class RewardOption {
    private String type;        // CREDITS, HACK, SHIELD, DOUBLE_NEXT, etc.
    private String label;       // Display text
    private String description; // Detailed description
    private Integer value;      // Value associated (e.g., credit amount)
    private String iconUrl;     // Optional icon

    public RewardOption() {}

    public RewardOption(String type, String label, Integer value) {
        this.type = type;
        this.label = label;
        this.value = value;
    }

    public RewardOption(String type, String label, String description, Integer value) {
        this.type = type;
        this.label = label;
        this.description = description;
        this.value = value;
    }

    // Pre-defined reward options
    public static RewardOption earnCredits(int amount) {
        return new RewardOption("CREDITS", "Earn " + amount + " Credits",
            "Add credits to your score", amount);
    }

    public static RewardOption hackAttempt() {
        return new RewardOption("HACK", "Hack a Player",
            "Try to guess another player's secret code", 0);
    }

    public static RewardOption shield(int durationSeconds) {
        return new RewardOption("SHIELD", "Shield (" + durationSeconds + "s)",
            "Protect yourself from hacks", durationSeconds);
    }

    public static RewardOption doubleNext() {
        return new RewardOption("DOUBLE_NEXT", "Double Next",
            "Double your next correct answer reward", 2);
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
