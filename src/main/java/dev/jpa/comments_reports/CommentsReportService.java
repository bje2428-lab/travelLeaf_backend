package dev.jpa.comments_reports;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import dev.jpa.comments.CommentsService;
import dev.jpa.posts_reports.NotificationService;
import dev.jpa.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentsReportService {

    private final CommentsReportRepository repo;
    private final UserService userService;
    private final NotificationService notification;
    private final CommentsService commentsService;

    // -------------------------------------
    // 🔥 신고 등록 (중복 방지 포함)
    // -------------------------------------
    public CommentsReport create(CommentsReportDTO dto) {

        // ⭐ 같은 사람이 같은 댓글 중복 신고 방지
        if (repo.existsByReporterIdAndCommentId(dto.getReporterId(), dto.getCommentId())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "이미 신고한 댓글입니다"
            );
        }

        CommentsReport report = CommentsReport.builder()
                .reporterId(dto.getReporterId())
                .reportCategory(dto.getReportCategory())
                .reason(dto.getReason())
                .evidenceUrl(dto.getEvidenceUrl())
                .status("PENDING")
                .commentId(dto.getCommentId())
                .build();

        notification.notifyAdmins("새 댓글 신고 접수됨. commentId=" + dto.getCommentId());
        return repo.save(report);
    }

    // 전체 조회
    public List<CommentsReport> findAll() {
        return repo.findAll();
    }

    public CommentsReport findOne(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 존재하지 않음"));
    }

    public List<CommentsReport> findByComment(Long commentId) {
        return repo.findByCommentId(commentId);
    }

    public List<CommentsReport> findByReporter(String reporterId) {
        return repo.findByReporterId(reporterId);
    }

    // -------------------------------------
    // 🔥 관리자 검색 + 페이징
    // -------------------------------------
    public Page<CommentsReportDTO> searchReports(
        String status,
        String keyword,
        int page,
        int size
) {
    if (status == null) status = "";
    if (keyword == null) keyword = "";

    Pageable pageable = PageRequest.of(page, size);

    Page<CommentsReport> result =
            repo.findByStatusContainingAndReasonContainingOrderByReportIdDesc(
                    status, keyword, pageable
            );

    return result.map(r -> {
        CommentsReportDTO dto = new CommentsReportDTO();
        dto.setReportId(r.getReportId());
        dto.setReporterId(r.getReporterId());
        dto.setReportCategory(r.getReportCategory());
        dto.setReason(r.getReason());
        dto.setEvidenceUrl(r.getEvidenceUrl());
        dto.setStatus(r.getStatus());
        dto.setCommentId(r.getCommentId());
        return dto;
    });
}


    /**
     * 🔥 관리자 신고 처리
     * APPROVED → 댓글 삭제 + 해당 댓글 모든 신고 삭제
     * REJECTED → 해당 신고만 삭제
     * IN_REVIEW → 상태만 변경
     */
    @Transactional
    public CommentsReport updateStatus(Long reportId, String status, String adminId) {

        int grade = userService.getUserGrade(adminId);
        if (grade != 2) {
            throw new ResponseStatusException(FORBIDDEN, "관리자만 처리 가능합니다");
        }

        CommentsReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        // 다른 관리자 처리중이면 막기
        if ("IN_REVIEW".equals(report.getStatus()) &&
                !adminId.equals(report.getAdminId())) {
            throw new ResponseStatusException(CONFLICT, "이미 다른 관리자가 처리중입니다");
        }

        // 처음 처리 시작이면 IN_REVIEW 지정
        if ("PENDING".equals(report.getStatus())) {
            report.setStatus("IN_REVIEW");
            report.setAdminId(adminId);
        }

        // -----------------------------
        // ⭐ APPROVED
        // 댓글 삭제 + 해당 댓글 신고 전부 삭제
        // -----------------------------
        if ("APPROVED".equals(status)) {

          Long rootCommentId = report.getCommentId();

          try {
              // 1️⃣ 부모 댓글 + 대댓글 ID 전부 조회
              List<Long> threadIds =
                      commentsService.findThreadCommentIds(rootCommentId);

              // 2️⃣ 이 댓글들에 대한 모든 신고 삭제
              if (!threadIds.isEmpty()) {
                  repo.deleteByCommentIds(threadIds);
              }

              // 3️⃣ 댓글 트리 삭제 (부모 + 대댓글)
              commentsService.adminDelete(rootCommentId, adminId, 2);

              System.out.println("🚨 승인 처리 → 댓글 트리 + 신고 삭제 완료 rootId=" + rootCommentId);

          } catch (Exception e) {
              throw new ResponseStatusException(
                      INTERNAL_SERVER_ERROR,
                      "댓글 승인 처리 중 오류",
                      e
              );
          }

          notification.sendReportProcessed(report.getReporterId(), reportId, "APPROVED");
          return report;
      }


        // -----------------------------
        // ⭐ REJECTED
        // 해당 신고만 삭제
        // -----------------------------
        if ("REJECTED".equals(status)) {
            repo.deleteById(reportId);
            notification.sendReportProcessed(report.getReporterId(), reportId, "REJECTED");
            return report;
        }

        // -----------------------------
        // ⭐ IN_REVIEW
        // -----------------------------
        report.setStatus(status);
        report.setAdminId(adminId);
        report.setProcessedAt(new Timestamp(System.currentTimeMillis()));

        CommentsReport saved = repo.save(report);

        notification.sendReportProcessed(report.getReporterId(), reportId, status);

        return saved;
    }

    // 관리자 강제 댓글 삭제
    public void deleteReportedComment(Long reportId, String adminId) {

        int grade = userService.getUserGrade(adminId);
        if (grade != 2) {
            throw new ResponseStatusException(FORBIDDEN, "관리자만 가능합니다");
        }

        CommentsReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        commentsService.adminDelete(
                report.getCommentId(),
                adminId,
                2
        );
    }

    // 신고 삭제
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
