package com.heronix.edu.client.multiplayer.dto;

/**
 * Represents an avatar that players can choose in Code Breaker.
 * Each avatar has unique bonuses.
 */
public class AvatarInfo {
    private String avatarId;
    private String name;
    private String emoji;
    private String bonus;
    private String bonusDescription;

    public AvatarInfo() {}

    public AvatarInfo(String avatarId, String name, String emoji, String bonus, String bonusDescription) {
        this.avatarId = avatarId;
        this.name = name;
        this.emoji = emoji;
        this.bonus = bonus;
        this.bonusDescription = bonusDescription;
    }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getBonus() { return bonus; }
    public void setBonus(String bonus) { this.bonus = bonus; }

    public String getBonusDescription() { return bonusDescription; }
    public void setBonusDescription(String bonusDescription) { this.bonusDescription = bonusDescription; }

    /**
     * Get predefined avatars for Code Breaker game.
     */
    public static AvatarInfo[] getAvailableAvatars() {
        return new AvatarInfo[] {
            new AvatarInfo("CLEVER_CAT", "Clever Cat", "🐱", "BONUS_CREDITS", "+10 bonus credits per correct answer"),
            new AvatarInfo("MIGHTY_BEAR", "Mighty Bear", "🐻", "HACK_BONUS", "1.5x credits when hacking"),
            new AvatarInfo("SHIELD_TURTLE", "Shield Turtle", "🐢", "PROTECTION", "30% chance to block hacks"),
            new AvatarInfo("QUICK_RABBIT", "Quick Rabbit", "🐰", "SPEED_BONUS", "+5 seconds on timed questions"),
            new AvatarInfo("WISE_OWL", "Wise Owl", "🦉", "HINT_BONUS", "Start with 1 free code hint"),
            new AvatarInfo("LUCKY_FOX", "Lucky Fox", "🦊", "LUCK_BONUS", "25% chance for double rewards")
        };
    }
}
