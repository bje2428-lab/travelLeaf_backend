package dev.jpa.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyOptionResultDTO {

    private Long optionId;
    private String optionText;
    private Long count;   // 선택 횟수
}
