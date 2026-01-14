package com.heronix.edu.server.dto.game;

/**
 * Request from player selecting a reward after answering correctly.
 */
public class RewardSelectionRequest {
    private String rewardType;  // CREDITS, HACK, SHIELD, DOUBLE_NEXT

    public RewardSelectionRequest() {}

    public RewardSelectionRequest(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }
}
