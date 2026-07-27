package dev.jpa.posts_reports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "POSTS_REPORTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_reports_seq")
    @SequenceGenerator(
            name = "posts_reports_seq",
            sequenceName = "SEQ_POSTS_REPORTS",
            allocationSize = 1
    )
    @Column(name = "report_id")
    private Long reportId;

    // 신고자 (사용자 or 시스템)
    @Column(name = "reporter_id", nullable = false)
    private String reporterId;

    // 처리 관리자
    @Column(name = "user_id")
    private String adminId;

    // 신고 분류 (LLM 결과도 여기 사용)
    @Column(name = "report_category")
    private String reportCategory; // INSULT / HATE / AD / SPAM / ...

    // 신고 상세 사유 (LLM 분석 요약 가능)
    @Column(name = "reason")
    private String reason;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    // PENDING / IN_REVIEW / APPROVED / REJECTED / AUTO_FLAGGED
    @Column(name = "status")
    private String status;

    /* ==========================
       🤖 AI 자동 감지 컬럼
    ========================== */

    // LLM 악성도 점수 (0.00 ~ 1.00)
    @Column(name = "ai_score")
    private Double aiScore;

    // 사용한 LLM 모델명
    @Column(name = "ai_model", length = 50)
    private String aiModel;

    // AI 자동 신고 여부 (Y / N)
    @Column(name = "ai_detected", length = 1)
    private String aiDetected;

    /* ==========================
       시간 컬럼
    ========================== */

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.sql.Timestamp createdAt;

    @Column(name = "processed_at")
    private java.sql.Timestamp processedAt;

    // 신고 대상 게시글
    @Column(name = "post_id", nullable = false)
    private Long postId;
}
