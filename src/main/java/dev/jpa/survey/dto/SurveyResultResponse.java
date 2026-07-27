package dev.jpa.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResultResponse {

    private Long surveyId;
    private List<SurveyQuestionResultDTO> questions;
}
