package dev.jpa.review;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import dev.jpa.posts_reports.NotificationService;
import dev.jpa.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewCommentReportService {

    private final ReviewCommentReportRepository repo;
    private final UserService userService;
    private final NotificationService notification;

    // ✅ 댓글 강제삭제용
    private final ReviewCommentService reviewCommentService;

    public ReviewCommentReport create(ReviewCommentReportDTO dto) {

        if (dto.getReporterId() == null || dto.getReporterId().isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "reporterId 필요");
        if (dto.getCommentId() == null)
            throw new ResponseStatusException(BAD_REQUEST, "commentId 필요");

        if (repo.existsByReporterIdAndCommentId(dto.getReporterId(), dto.getCommentId()))
            throw new ResponseStatusException(CONFLICT, "이미 신고한 댓글입니다");

        ReviewCommentReport report = ReviewCommentReport.builder()
                .reporterId(dto.getReporterId())
                .reportCategory(dto.getReportCategory())
                .reason(dto.getReason())
                .evidenceUrl(dto.getEvidenceUrl())
                .status("PENDING")
                .commentId(dto.getCommentId())
                .build();

        notification.notifyAdmins("새 리뷰댓글 신고 접수됨. commentId=" + dto.getCommentId());
        return repo.save(report);
    }

    public List<ReviewCommentReport> findAll() {
        return repo.findAll();
    }

    public ReviewCommentReport findOne(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 존재하지 않음"));
    }

    public List<ReviewCommentReport> findByComment(Long commentId) {
        return repo.findByCommentId(commentId);
    }

    public List<ReviewCommentReport> findByReporter(String reporterId) {
        return repo.findByReporterId(reporterId);
    }

    /**
     * ✅ 검색 + 페이징 (CLOB 대응)
     */
    public Page<ReviewCommentReportDTO> searchReports(String status, String keyword, int page, int size) {
        if (status == null) status = "";
        if (keyword == null) keyword = "";

        Pageable pageable = PageRequest.of(page, size);

        return repo.searchReportsNative(status, keyword, pageable)
                .map(r -> ReviewCommentReportDTO.builder()
                        .reportId(r.getReportId())
                        .reporterId(r.getReporterId())
                        .reportCategory(r.getReportCategory())
                        .reason(r.getReason())
                        .evidenceUrl(r.getEvidenceUrl())
                        .status(r.getStatus())
                        .commentId(r.getCommentId())
                        .build());
    }

    /**
     * ✅ 상태 변경 (관리자만)
     * - PENDING -> IN_REVIEW 선점
     * - APPROVED: 댓글 강제삭제 + 해당 commentId 신고들 APPROVED 처리
     * - REJECTED: 상태만 REJECTED 저장
     */
    @Transactional
    public ReviewCommentReport updateStatus(Long reportId, String status, String managerId) {

        int grade = userService.getUserGrade(managerId);
        if (grade != 2) throw new ResponseStatusException(FORBIDDEN, "grade=2만 처리 가능");

        if (status == null || status.isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "status 필요");

        ReviewCommentReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        // ✅ 선점 잠금
        if ("IN_REVIEW".equals(report.getStatus())
                && report.getManagerId() != null
                && !managerId.equals(report.getManagerId())) {
            throw new ResponseStatusException(CONFLICT, "이미 다른 처리자가 처리중입니다");
        }

        // ✅ PENDING이면 선점 처리
        if ("PENDING".equals(report.getStatus())) {
            report.setStatus("IN_REVIEW");
            report.setManagerId(managerId);
            repo.save(report);
        }

        // ===== 승인(APPROVED) =====
        if ("APPROVED".equals(status)) {
            Long commentId = report.getCommentId();
            LocalDateTime now = LocalDateTime.now();

            try {
                // ✅ 댓글 강제삭제
                reviewCommentService.forceDeleteByManager(commentId, managerId);

                // ✅ 같은 commentId의 신고들 전체 APPROVED로 기록
                repo.updateAllStatusByCommentId(commentId, "APPROVED", managerId, now);

            } catch (Exception e) {
                throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "리뷰댓글 승인 처리 중 오류", e);
            }

            notification.sendReportProcessed(report.getReporterId(), reportId, "APPROVED");
            return repo.findById(reportId).orElse(report);
        }

        // ===== 거절(REJECTED) =====
        if ("REJECTED".equals(status)) {
            report.setStatus("REJECTED");
            report.setManagerId(managerId);
            report.setProcessedAt(LocalDateTime.now());

            ReviewCommentReport saved = repo.save(report);
            notification.sendReportProcessed(report.getReporterId(), reportId, "REJECTED");
            return saved;
        }

        // ===== 기타 상태 변경 =====
        report.setStatus(status);
        report.setManagerId(managerId);
        report.setProcessedAt(LocalDateTime.now());

        ReviewCommentReport saved = repo.save(report);
        notification.sendReportProcessed(report.getReporterId(), reportId, status);
        return saved;
    }

    /**
     * ✅ 강제삭제(승인) 전용
     */
    @Transactional
    public void forceDeleteReportedComment(Long reportId, String managerId) {
        int grade = userService.getUserGrade(managerId);
        if (grade != 2) throw new ResponseStatusException(FORBIDDEN, "grade=2만 가능");

        ReviewCommentReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        Long commentId = report.getCommentId();

        // ✅ 댓글 강제삭제
        reviewCommentService.forceDeleteByManager(commentId, managerId);

        // ✅ 승인 기록도 남기려면(권장)
        LocalDateTime now = LocalDateTime.now();
        repo.updateAllStatusByCommentId(commentId, "APPROVED", managerId, now);

        notification.sendReportProcessed(report.getReporterId(), reportId, "APPROVED");
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
