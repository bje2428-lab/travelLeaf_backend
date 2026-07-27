package dev.jpa.survey.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SurveySubmitRequest {

    private Long surveyId;
    private Long userNo;
    
 // ✅ 추가
    private Long rewardId;
    private Integer surveyReward;
    private String email;
    
    private List<SurveyAnswerDTO> answers;
}
