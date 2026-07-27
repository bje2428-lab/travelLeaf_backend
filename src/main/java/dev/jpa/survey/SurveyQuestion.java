package dev.jpa.survey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Entity
@Table(name = "SURVEY_QUESTION")
@Getter @Setter
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_q_seq")
    @SequenceGenerator(name = "survey_q_seq", sequenceName = "SURVEY_Q_SEQ", allocationSize = 1)
    private Long questionId;

    private Long surveyId;
    private Integer questionNo;
    private String questionText;
    private String questionType; // SINGLE / MULTI / TEXT / SCORE
    private String requiredYn;
}
