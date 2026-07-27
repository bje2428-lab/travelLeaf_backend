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
public class SurveyQuestionResultDTO {

    private Long questionId;
    private String questionText;
    private String questionType;   // SINGLE / TEXT
    private List<SurveyOptionResultDTO> options;

    // TEXT 응답 요약용
    private List<String> textAnswers;
}
