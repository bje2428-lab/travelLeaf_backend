package dev.jpa.survey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SURVEY_OPTION")
@Getter
@Setter
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @Column(nullable = false)
    private Long questionId;

    private Integer optionNo;
    private String optionText;

    // ⭐ 추가
    private Integer scoreValue;
}
