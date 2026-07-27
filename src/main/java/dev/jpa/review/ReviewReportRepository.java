package dev.jpa.review;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    List<ReviewReport> findByReviewId(Long reviewId);

    List<ReviewReport> findByReporterId(String reporterId);

    boolean existsByReporterIdAndReviewId(String reporterId, Long reviewId);

    void deleteByReviewId(Long reviewId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewReport r WHERE r.reviewId IN :reviewIds")
    void deleteByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    // ✅ DB 컬럼 13개 정확히 일치
    String REPORT_COLS =
            "REPORT_ID, REPORTER_ID, USER_ID, REPORT_CATEGORY, REASON, " +
            "EVIDENCE_URL, STATUS, AI_SCORE, AI_MODEL, AI_DETECTED, " +
            "CREATED_AT, PROCESSED_AT, REVIEW_ID";

    // ✅ CLOB(REASON) 검색: DBMS_LOB.INSTR
    @Query(
        value =
            "SELECT " + REPORT_COLS + " " +
            "FROM REVIEW_REPORTS " +
            "WHERE (:status IS NULL OR :status = '' OR STATUS LIKE '%' || :status || '%') " +
            "  AND (:keyword IS NULL OR :keyword = '' OR DBMS_LOB.INSTR(REASON, :keyword) > 0) " +
            "ORDER BY REPORT_ID DESC",
        countQuery =
            "SELECT COUNT(*) " +
            "FROM REVIEW_REPORTS " +
            "WHERE (:status IS NULL OR :status = '' OR STATUS LIKE '%' || :status || '%') " +
            "  AND (:keyword IS NULL OR :keyword = '' OR DBMS_LOB.INSTR(REASON, :keyword) > 0)",
        nativeQuery = true
    )
    Page<ReviewReport> searchReportsNative(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ✅ 같은 reviewId의 신고들 일괄 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ReviewReport r
           SET r.status = :status,
               r.managerId = :managerId,
               r.processedAt = :processedAt
         WHERE r.reviewId = :reviewId
    """)
    int updateAllStatusByReviewId(
            @Param("reviewId") Long reviewId,
            @Param("status") String status,
            @Param("managerId") String managerId,
            @Param("processedAt") Timestamp processedAt
    );
}
