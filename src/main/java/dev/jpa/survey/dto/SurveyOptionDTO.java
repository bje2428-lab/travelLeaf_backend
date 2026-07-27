package dev.jpa.survey.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyOptionDTO {

    private Long optionId;
    private Integer optionNo;
    private String optionText;

    // ⭐ 이 필드가 없어서 에러 났음
    private Integer scoreValue;
}
