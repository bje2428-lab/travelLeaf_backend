package dev.jpa.survey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SURVEY")
@Getter @Setter
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_seq")
    @SequenceGenerator(
        name = "survey_seq",
        sequenceName = "SURVEY_SEQ",
        allocationSize = 1
    )
    @Column(name = "SURVEY_ID")
    private Long surveyId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SURVEY_TYPE")
    private String surveyType;

    @Column(name = "EST_TIME_MIN")
    private Integer estTimeMin;

    @Column(name = "REWARD_POINT")
    private Integer rewardPoint;

    // 🔥 이거 중요
    @Column(name = "ACTIVE_YN")
    private String activeYn;

    // 🔥 이거 중요
    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
