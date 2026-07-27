package dev.jpa.comments_reports;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface CommentsReportRepository extends JpaRepository<CommentsReport, Long> {

    // ✅ 댓글 ID 목록으로 신고 삭제 (안전한 방식)
    @Modifying
    @Transactional
    @Query("DELETE FROM CommentsReport r WHERE r.commentId IN :commentIds")
    void deleteByCommentIds(@Param("commentIds") List<Long> commentIds);

    // 특정 댓글 신고 목록
    List<CommentsReport> findByCommentId(Long commentId);

    // 특정 유저 신고 목록
    List<CommentsReport> findByReporterId(String reporterId);

    // ⭐⭐ 검색 + 페이징
    Page<CommentsReport> findByStatusContainingAndReasonContainingOrderByReportIdDesc(
            String status,
            String keyword,
            Pageable pageable
    );

    // ⭐ 중복 신고 방지
    boolean existsByReporterIdAndCommentId(String reporterId, Long commentId);
}
