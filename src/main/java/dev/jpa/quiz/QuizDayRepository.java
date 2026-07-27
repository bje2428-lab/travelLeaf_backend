package dev.jpa.quiz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizDayRepository extends JpaRepository<QuizDay, Long> {

    /* n일차 퀴즈 목록 조회 (문제 풀기용) */
    @Query("""
        SELECT q
        FROM QuizDay d
        JOIN Quiz q ON q.quizId = d.quizId
        WHERE d.dayNo = :dayNo
          AND d.activeYn = 'Y'
        ORDER BY d.sortOrder
    """)
    List<Quiz> findQuizByDay(@Param("dayNo") int dayNo);

    /* n일차 전체 퀴즈 개수 */
    @Query("""
        SELECT COUNT(d)
        FROM QuizDay d
        WHERE d.dayNo = :dayNo
          AND d.activeYn = 'Y'
    """)
    int countQuizByDay(@Param("dayNo") int dayNo);
    void deleteByQuizId(Long quizId);
}
