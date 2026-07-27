package dev.jpa.survey.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SurveyDetailDTO {

    private Long surveyId;
    private String title;
    private String description;
    private String surveyType;

    // ⭐ 이 필드가 없어서 에러 났음
    private Integer rewardExp;

    private Integer estTimeMin;
    private List<SurveyQuestionDTO> questions;
}
