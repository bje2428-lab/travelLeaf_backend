package dev.jpa.survey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Entity
@Table(name = "SURVEY_RESPONSE")
@Getter @Setter
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_resp_seq")
    @SequenceGenerator(name = "survey_resp_seq", sequenceName = "SURVEY_RESP_SEQ", allocationSize = 1)
    private Long responseId;

    private Long surveyId;
    private Long userNo;
    private String completedYn;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;
}
