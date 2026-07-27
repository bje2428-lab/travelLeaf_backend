package dev.jpa.survey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("""
        SELECT s
        FROM Survey s
        WHERE TRIM(s.activeYn) = :activeYn
    """)
    Page<Survey> findActive(
        @Param("activeYn") String activeYn,
        Pageable pageable
    );

    /* =========================
       객관식 결과 집계
    ========================= */
    @Query("""
        SELECT
            q.questionId,
            q.questionText,
            o.optionId,
            o.optionText,
            COUNT(a.answerId)
        FROM SurveyQuestion q
        JOIN SurveyOption o ON o.questionId = q.questionId
        LEFT JOIN SurveyAnswer a
            ON a.optionId = o.optionId
        JOIN SurveyResponse r
            ON r.responseId = a.responseId
        WHERE q.surveyId = :surveyId
        GROUP BY
            q.questionId,
            q.questionText,
            o.optionId,
            o.optionText
        ORDER BY q.questionId, o.optionId
    """)
    List<Object[]> findSurveyChoiceResult(@Param("surveyId") Long surveyId);

    /* =========================
       TEXT 응답
    ========================= */
    @Query("""
        SELECT
            q.questionId,
            q.questionText,
            a.answerText
        FROM SurveyQuestion q
        JOIN SurveyAnswer a
            ON a.questionId = q.questionId
        JOIN SurveyResponse r
            ON r.responseId = a.responseId
        WHERE q.surveyId = :surveyId
          AND q.questionType = 'TEXT'
          AND a.answerText IS NOT NULL
        ORDER BY q.questionId
    """)
    List<Object[]> findSurveyTextAnswers(@Param("surveyId") Long surveyId);
}
