package dev.jpa.posts_reports;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsReportRepository extends JpaRepository<PostsReport, Long> {

    // 특정 게시글 신고 목록
    List<PostsReport> findByPostId(Long postId);

    // 특정 유저 신고 목록
    List<PostsReport> findByReporterId(String reporterId);

    // ⭐ 검색 + 페이징 (댓글 신고와 동일)
    Page<PostsReport> findByStatusContainingAndReasonContainingOrderByReportIdDesc(
            String status,
            String keyword,
            Pageable pageable
    );

    // ⭐ 중복 신고 방지
    boolean existsByReporterIdAndPostId(String reporterId, Long postId);
}
