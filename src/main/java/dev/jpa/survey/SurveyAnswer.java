package dev.jpa.survey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Entity
@Table(name = "SURVEY_ANSWER")
@Getter @Setter
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_ans_seq")
    @SequenceGenerator(name = "survey_ans_seq", sequenceName = "SURVEY_ANS_SEQ", allocationSize = 1)
    private Long answerId;

    private Long responseId;
    private Long questionId;
    private Long optionId;
    private String answerText;
    private Integer scoreValue;
}
