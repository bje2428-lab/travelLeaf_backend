package dev.jpa.posts_reports;

import static org.springframework.http.HttpStatus.*;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import dev.jpa.posts.PostsService;
import dev.jpa.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostsReportService {

    private final PostsReportRepository repo;
    private final UserService userService;
    private final PostsService postsService;

    // -------------------------------------
    // 🔥 신고 등록 (댓글신고와 동일)
    // -------------------------------------
    @Transactional
    public PostsReport create(PostsReportDTO dto) {

        if (repo.existsByReporterIdAndPostId(dto.getReporterId(), dto.getPostId())) {
            throw new ResponseStatusException(CONFLICT, "이미 신고한 게시글입니다");
        }

        PostsReport report = PostsReport.builder()
                .reporterId(dto.getReporterId())
                .reportCategory(dto.getReportCategory())
                .reason(dto.getReason())
                .evidenceUrl(dto.getEvidenceUrl())
                .status("PENDING")
                .postId(dto.getPostId())
                .build();

        return repo.save(report);
    }

    // -------------------------------------
    // 조회
    // -------------------------------------
    public List<PostsReport> findAll() {
        return repo.findAll();
    }

    public PostsReport findOne(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 존재하지 않음"));
    }

    public List<PostsReport> findByPost(Long postId) {
        return repo.findByPostId(postId);
    }

    public List<PostsReport> findByReporter(String reporterId) {
        return repo.findByReporterId(reporterId);
    }

    // -------------------------------------
    // 🔥 관리자 검색 (댓글신고와 동일)
    // -------------------------------------
    public Page<PostsReportDTO> searchReports(String status, String keyword, int page, int size) {

        if (status == null) status = "";
        if (keyword == null) keyword = "";

        Pageable pageable = PageRequest.of(page, size);

        Page<PostsReport> result =
                repo.findByStatusContainingAndReasonContainingOrderByReportIdDesc(
                        status, keyword, pageable
                );

        return result.map(r -> {
            PostsReportDTO dto = new PostsReportDTO();
            dto.setReportId(r.getReportId());
            dto.setReporterId(r.getReporterId());
            dto.setReportCategory(r.getReportCategory());
            dto.setReason(r.getReason());
            dto.setEvidenceUrl(r.getEvidenceUrl());
            dto.setStatus(r.getStatus());
            dto.setPostId(r.getPostId());
            return dto;
        });
    }

    /**
     * 🔥 관리자 신고 처리
     * APPROVED → 게시글 삭제 (PostsService가 댓글/댓글신고 정리까지 전담)
     * REJECTED → 해당 신고만 삭제
     * IN_REVIEW → 상태만 변경
     */
    @Transactional
    public PostsReport updateStatus(Long reportId, String status, String adminId) {

        int grade = userService.getUserGrade(adminId);
        if (grade != 2) {
            throw new ResponseStatusException(FORBIDDEN, "관리자만 처리 가능합니다");
        }

        PostsReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        // 다른 관리자 처리중이면 막기
        if ("IN_REVIEW".equals(report.getStatus())
                && report.getAdminId() != null
                && !adminId.equals(report.getAdminId())) {
            throw new ResponseStatusException(CONFLICT, "이미 다른 관리자가 처리중입니다");
        }

        // 처음 처리 시작이면 IN_REVIEW 지정
        if ("PENDING".equals(report.getStatus())) {
            report.setStatus("IN_REVIEW");
            report.setAdminId(adminId);
            report.setProcessedAt(new Timestamp(System.currentTimeMillis()));
            repo.save(report);
        }

        // -----------------------------
        // ⭐ APPROVED
        // 게시글 삭제 (게시글 신고 deleteAll은 여기서 하지 않음!)
        // -----------------------------
        if ("APPROVED".equals(status)) {

          Long postId = report.getPostId();

          // 1️⃣ 게시글 먼저 삭제 (실패하면 예외 발생 → 신고도 유지)
          postsService.adminDelete(postId, adminId);

          // 2️⃣ 이 게시글에 대한 모든 신고 삭제
          repo.deleteAll(repo.findByPostId(postId));

          return report;   // 더 이상 save 하지 않음
      }


        // -----------------------------
        // ⭐ IN_REVIEW (또는 기타 상태)
        // -----------------------------
        report.setStatus(status);
        report.setAdminId(adminId);
        report.setProcessedAt(new Timestamp(System.currentTimeMillis()));

        return repo.save(report);
    }

    // 관리자 강제 게시글 삭제
    @Transactional
    public void deleteReportedPost(Long reportId, String adminId) {

        int grade = userService.getUserGrade(adminId);
        if (grade != 2) {
            throw new ResponseStatusException(FORBIDDEN, "관리자만 가능합니다");
        }

        PostsReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        postsService.adminDelete(report.getPostId(), adminId);
    }

    // 신고 삭제
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
