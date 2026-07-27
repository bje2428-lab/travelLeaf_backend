package dev.jpa.survey.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyAnswerDTO {

    private Long questionId;

    // 객관식 (SINGLE / MULTI)
    private Long optionId;

    // 주관식 (TEXT)
    private String answerText;

    // 점수형 (SCORE)
    private Integer scoreValue;
}
