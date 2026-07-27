package dev.jpa.review;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW_COMMENT_REPORTS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_comment_reports_seq")
    @SequenceGenerator(
            name = "review_comment_reports_seq",
            sequenceName = "SEQ_REVIEW_COMMENT_REPORTS",
            allocationSize = 1
    )
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "reporter_id", nullable = false)
    private String reporterId;

    // 처리자(grade=2) - DB 컬럼은 user_id
    @Column(name = "user_id")
    private String managerId;

    @Column(name = "report_category")
    private String reportCategory;

    // Oracle CLOB
    @Lob
    @Column(name = "reason")
    private String reason;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    @Column(name = "status")
    private String status; // PENDING/IN_REVIEW/APPROVED/REJECTED

    @Column(name = "ai_score")
    private Double aiScore;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "ai_detected", length = 1)
    private String aiDetected;

    // ✅ LocalDateTime
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ LocalDateTime
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;
}
