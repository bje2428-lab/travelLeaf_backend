package dev.jpa.review;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "REVIEW")
@SequenceGenerator(
        name = "review_seq_generator",
        sequenceName = "REVIEW_SEQ",
        allocationSize = 1
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_seq_generator")
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @Column(name = "CITY", nullable = false, length = 20)
    private String city;

    @Column(name = "DISTRICT", nullable = false, length = 20)
    private String district;

    @Column(name = "PLACE_NAME", nullable = false, length = 100)
    private String placeName;

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "RATING", nullable = false)
    private Integer rating;

    @Column(name = "CONTENT", nullable = false, length = 2000)
    private String content;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @Column(name = "TOXIC_SCORE")
    private Double toxicScore;

    @Column(name = "FLAG_REASON", length = 50)
    private String flagReason;

    @Column(name = "SENTIMENT", length = 50)
    private String sentiment;

    @Column(name = "MODERATED_AT")
    private LocalDateTime moderatedAt;

    @Column(name = "AI_SUMMARY", length = 1000)
    private String aiSummary;

    @Column(name = "AI_KEYWORDS", length = 500)
    private String aiKeywords;

    @Column(name = "AI_SUMMARY_AT")
    private LocalDateTime aiSummaryAt;

    // ✅ 추가: 소프트 삭제 컬럼
    @Column(name = "IS_DELETED", nullable = false)
    private Integer isDeleted;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    // ✅ 핵심: INSERT 전에 null이면 0으로 강제 세팅 (ORA-01400 방지)
    @PrePersist
    public void prePersist() {
        if (isDeleted == null) isDeleted = 0;
    }
}
