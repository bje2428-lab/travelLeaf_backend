package dev.jpa.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "QUIZ_ATTEMPT")
@Getter
@Setter
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quiz_attempt_seq")
    @SequenceGenerator(name = "quiz_attempt_seq", sequenceName = "QUIZ_ATTEMPT_SEQ", allocationSize = 1)
    @Column(name = "ATTEMPT_ID")
    private Long attemptId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "QUIZ_ID")
    private Long quizId;

    @Column(name = "DAY_NO")
    private int dayNo;

    @Column(name = "IS_CORRECT", length = 1)
    private String isCorrect; // Y/N

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ATTEMPTED_AT")
    private Date attemptedAt;

    /* =========================
       ✅ LLM 결과 저장 컬럼들
    ========================= */

    @Lob
    @Column(name = "COACH_TEXT")
    private String coachText; // 5분 복습 코치 텍스트

    @Column(name = "RECOMMEND_QUIZ_IDS", length = 500)
    private String recommendQuizIds; // 예) "12,55,90"

    @Column(name = "RECOMMEND_REASON", length = 1000)
    private String recommendReason; // 추천 이유

    @Column(name = "STRENGTH_TEXT", length = 500)
    private String strengthText; // 잘한 점

    @Column(name = "IMPROVE_TEXT", length = 500)
    private String improveText; // 개선점

    @Column(name = "NEXT_ACTION_TEXT", length = 500)
    private String nextActionText; // 다음 행동

    /* =========================
       ✅ 비동기 상태 컬럼들
    ========================= */

    @Column(name = "AI_STATUS", length = 20)
    private String aiStatus; // PENDING / DONE / FAILED

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "AI_UPDATED_AT")
    private Date aiUpdatedAt;
}
