package dev.jpa.review;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "REVIEW_REPORTS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_reports_seq")
    @SequenceGenerator(
            name = "review_reports_seq",
            sequenceName = "SEQ_REVIEW_REPORTS",
            allocationSize = 1
    )
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Column(name = "REPORTER_ID", nullable = false, length = 50)
    private String reporterId;

    // 처리자(grade=2 사용자) - DB 컬럼은 USER_ID
    @Column(name = "USER_ID", length = 50)
    private String managerId;

    @Column(name = "REPORT_CATEGORY", length = 50)
    private String reportCategory;

    // DB가 CLOB이면 @Lob 권장
    @Lob
    @Column(name = "REASON")
    private String reason;

    @Column(name = "EVIDENCE_URL", length = 500)
    private String evidenceUrl;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "AI_SCORE")
    private Double aiScore;

    @Column(name = "AI_MODEL", length = 50)
    private String aiModel;

    @Column(name = "AI_DETECTED", length = 1)
    private String aiDetected; // Y/N

    // DB: DATE -> Timestamp 매핑 가능
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "PROCESSED_AT")
    private Timestamp processedAt;

    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;
}
