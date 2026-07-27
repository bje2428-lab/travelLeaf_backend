package dev.jpa.survey.dto;

import dev.jpa.survey.Survey;
import lombok.Getter;

@Getter
public class SurveyListDTO {

    private Long surveyId;
    private String title;
    private String surveyType;
    private Integer rewardPoint;   // ⭐ wrapper
    private Integer estTimeMin;     // ⭐ wrapper

    public SurveyListDTO(Survey s) {
        this.surveyId = s.getSurveyId();
        this.title = s.getTitle();
        this.surveyType = s.getSurveyType();
        this.rewardPoint = s.getRewardPoint();
        this.estTimeMin = s.getEstTimeMin();
    }
}
