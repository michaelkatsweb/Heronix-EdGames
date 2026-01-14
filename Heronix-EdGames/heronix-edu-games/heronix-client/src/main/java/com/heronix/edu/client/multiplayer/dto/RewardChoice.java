package com.heronix.edu.client.multiplayer.dto;

/**
 * Represents a reward option after answering correctly.
 */
public class RewardChoice {
    private String type;
    private int value;
    private String description;

    public RewardChoice() {}

    public RewardChoice(String type, int value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Get display icon for reward type.
     */
    public String getIcon() {
        return switch (type) {
            case "CREDITS" -> "💰";
            case "SHIELD" -> "🛡";
            case "CODE_HINT" -> "🔍";
            default -> "🎁";
        };
    }
}
