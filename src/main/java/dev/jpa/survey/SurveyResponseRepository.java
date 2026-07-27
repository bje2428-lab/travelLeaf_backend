package dev.jpa.survey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository
        extends JpaRepository<SurveyResponse, Long> {

    // 특정 유저가 특정 설문을 이미 했는지 체크 (선택)
    boolean existsBySurveyIdAndUserNo(Long surveyId, Long userNo);
}
