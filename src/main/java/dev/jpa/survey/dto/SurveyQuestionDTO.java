package dev.jpa.survey.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SurveyQuestionDTO {

    private Long questionId;
    private Integer questionNo;
    private String questionText;
    private String questionType;   // SINGLE / MULTI / TEXT / SCORE
    private String requiredYn;

    private List<SurveyOptionDTO> options;
}
