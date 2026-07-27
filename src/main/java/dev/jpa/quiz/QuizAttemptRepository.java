package dev.jpa.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Query("""
        SELECT COUNT(a)
        FROM QuizAttempt a
        WHERE a.userId = :userId
          AND a.dayNo = :dayNo
    """)
    int countSolvedByDay(
            @Param("userId") Long userId,
            @Param("dayNo") int dayNo
    );

    void deleteByQuizId(Long quizId);

    // ✅ 최신 attempt: DATE 정렬 꼬임 방지
    Optional<QuizAttempt> findTopByUserIdAndDayNoOrderByAttemptedAtDescAttemptIdDesc(Long userId, int dayNo);

    // ✅ (추가) 특정 퀴즈 기준 최신 attempt
    Optional<QuizAttempt> findTopByUserIdAndDayNoAndQuizIdOrderByAttemptIdDesc(Long userId, int dayNo, Long quizId);
}
