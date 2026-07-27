package dev.jpa.review;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewCommentReportRepository extends JpaRepository<ReviewCommentReport, Long> {

    List<ReviewCommentReport> findByCommentId(Long commentId);

    List<ReviewCommentReport> findByReporterId(String reporterId);

    boolean existsByReporterIdAndCommentId(String reporterId, Long commentId);

    void deleteByCommentId(Long commentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewCommentReport r WHERE r.commentId IN :commentIds")
    void deleteByCommentIds(@Param("commentIds") List<Long> commentIds);

    // ✅ native + pageable은 Oracle에서 ORA-17090 피하려면 positional binding(?1, ?2) 권장
    String REPORT_COLS =
            "REPORT_ID, REPORTER_ID, USER_ID, REPORT_CATEGORY, REASON, " +
            "EVIDENCE_URL, STATUS, AI_SCORE, AI_MODEL, AI_DETECTED, " +
            "CREATED_AT, PROCESSED_AT, COMMENT_ID";

    @Query(
        value =
            "SELECT " + REPORT_COLS + " " +
            "FROM REVIEW_COMMENT_REPORTS " +
            "WHERE ( ?1 IS NULL OR ?1 = '' OR STATUS LIKE '%' || ?1 || '%' ) " +
            "  AND ( ?2 IS NULL OR ?2 = '' OR DBMS_LOB.INSTR(REASON, ?2) > 0 ) " +
            "ORDER BY REPORT_ID DESC",
        countQuery =
            "SELECT COUNT(*) " +
            "FROM REVIEW_COMMENT_REPORTS " +
            "WHERE ( ?1 IS NULL OR ?1 = '' OR STATUS LIKE '%' || ?1 || '%' ) " +
            "  AND ( ?2 IS NULL OR ?2 = '' OR DBMS_LOB.INSTR(REASON, ?2) > 0 )",
        nativeQuery = true
    )
    Page<ReviewCommentReport> searchReportsNative(String status, String keyword, Pageable pageable);

    // ✅ 여기 핵심: LocalDateTime으로 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ReviewCommentReport r
           SET r.status = :status,
               r.managerId = :managerId,
               r.processedAt = :processedAt
         WHERE r.commentId = :commentId
    """)
    int updateAllStatusByCommentId(
            @Param("commentId") Long commentId,
            @Param("status") String status,
            @Param("managerId") String managerId,
            @Param("processedAt") LocalDateTime processedAt
    );
}
