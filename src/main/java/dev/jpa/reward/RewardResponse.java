package dev.jpa.reward;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RewardResponse {
    private int rewardValue;
    private int currentExp;
    private int currentLevel;
    private int prevLevel;
}

