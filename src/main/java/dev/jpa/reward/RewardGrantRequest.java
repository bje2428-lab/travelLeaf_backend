package dev.jpa.reward;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RewardGrantRequest {

    private Long userNo;
    private String rewardType;   // EXP
    private String sourceType;   // QUIZ / SURVEY
    private String sourceKey;    // QUIZ_DAY_1 / SURVEY_3

    // 퀴즈 전용
    private Integer totalCount;   // 4
    private Integer correctCount; // 0~4

    // 설문 전용
    private Integer surveyReward; // 30 or 50
    
    private String email;
    
    private Long rewardId;

}

