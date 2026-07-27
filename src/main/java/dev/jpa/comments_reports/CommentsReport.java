package dev.jpa.comments_reports;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "COMMENTS_REPORTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_reports_seq")
    @SequenceGenerator(
            name = "comments_reports_seq",
            sequenceName = "SEQ_COMMENTS_REPORTS",
            allocationSize = 1
    )
    @Column(name = "report_id")
    private Long reportId;

    // 신고자 (사용자 or SYSTEM)
    @Column(name = "reporter_id", nullable = false)
    private String reporterId;

    // 처리 관리자
    @Column(name = "user_id")
    private String adminId;

    // 신고 분류 (LLM 결과도 여기 사용)
    @Column(name = "report_category")
    private String reportCategory; // INSULT / HATE / AD / SPAM / ...

    // 신고 사유 상세
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

    // 신고 대상 댓글
    @Column(name = "comment_id", nullable = false)
    private Long commentId;
}
