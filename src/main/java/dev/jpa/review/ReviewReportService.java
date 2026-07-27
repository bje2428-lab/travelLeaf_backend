package dev.jpa.review;

import static org.springframework.http.HttpStatus.*;

import java.sql.Timestamp;
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
public class ReviewReportService {

    private final ReviewReportRepository repo;
    private final UserService userService;
    private final NotificationService notification;

    // ✅ 승인 시 리뷰 소프트삭제 + 연관 정리
    private final ReviewRepository reviewRepo;
    private final ReviewTagRepository tagRepo;
    private final ReviewCommentRepository reviewCommentRepo;
    private final ReviewCommentLikeRepository reviewCommentLikeRepo;

    public ReviewReport create(ReviewReportDTO dto) {

        if (dto.getReporterId() == null || dto.getReporterId().isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "reporterId 필요");
        if (dto.getReviewId() == null)
            throw new ResponseStatusException(BAD_REQUEST, "reviewId 필요");

        if (repo.existsByReporterIdAndReviewId(dto.getReporterId(), dto.getReviewId()))
            throw new ResponseStatusException(CONFLICT, "이미 신고한 리뷰입니다");

        ReviewReport report = ReviewReport.builder()
                .reporterId(dto.getReporterId())
                .reportCategory(dto.getReportCategory())
                .reason(dto.getReason())
                .evidenceUrl(dto.getEvidenceUrl())
                .status("PENDING")
                .reviewId(dto.getReviewId())
                .build();

        notification.notifyAdmins("새 리뷰 신고 접수됨. reviewId=" + dto.getReviewId());
        return repo.save(report);
    }

    public List<ReviewReport> findAll() { return repo.findAll(); }

    public ReviewReport findOne(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, "신고 존재하지 않음"));
    }

    public List<ReviewReport> findByReview(Long reviewId) { return repo.findByReviewId(reviewId); }

    public List<ReviewReport> findByReporter(String reporterId) { return repo.findByReporterId(reporterId); }

    /**
     * ✅ 신고 검색 + 페이징
     * - reason(CLOB) 대응: native query + DBMS_LOB.INSTR
     */
    public Page<ReviewReportDTO> searchReports(String status, String keyword, int page, int size) {
        if (status == null) status = "";
        if (keyword == null) keyword = "";

        Pageable pageable = PageRequest.of(page, size);

        return repo.searchReportsNative(status, keyword, pageable)
                .map(r -> ReviewReportDTO.builder()
                        .reportId(r.getReportId())
                        .reporterId(r.getReporterId())
                        .reportCategory(r.getReportCategory())
                        .reason(r.getReason())
                        .evidenceUrl(r.getEvidenceUrl())
                        .status(r.getStatus())
                        .reviewId(r.getReviewId())
                        .build());
    }

    @Transactional
    public ReviewReport updateStatus(Long reportId, String status, String managerId) {

        int grade = userService.getUserGrade(managerId);
        if (grade != 2) throw new ResponseStatusException(FORBIDDEN, "grade=2만 처리 가능");

        if (status == null || status.isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "status 필요");

        ReviewReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        // 다른 관리자가 선점(IN_REVIEW) 중이면 막기
        if ("IN_REVIEW".equals(report.getStatus())
                && report.getManagerId() != null
                && !managerId.equals(report.getManagerId())) {
            throw new ResponseStatusException(CONFLICT, "이미 다른 처리자가 처리중입니다");
        }

        // PENDING이면 선점 처리
        if ("PENDING".equals(report.getStatus())) {
            report.setStatus("IN_REVIEW");
            report.setManagerId(managerId);
            repo.save(report);
        }

        // ===== 승인(APPROVED) =====
        if ("APPROVED".equals(status)) {
            Long reviewId = report.getReviewId();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            try {
                // 1) 동일 reviewId 신고 전부 APPROVED 기록
                repo.updateAllStatusByReviewId(reviewId, "APPROVED", managerId, now);

                // 2) 연관 정리
                reviewCommentLikeRepo.deleteByReviewIdHard(reviewId);
                reviewCommentRepo.deleteByReviewIdHard(reviewId);
                tagRepo.deleteByReviewId(reviewId);

                // 3) 리뷰 소프트삭제
                reviewRepo.softDelete(reviewId);

            } catch (Exception e) {
                throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "리뷰 승인 처리 중 오류", e);
            }

            notification.sendReportProcessed(report.getReporterId(), reportId, "APPROVED");
            return repo.findById(reportId).orElse(report);
        }

        // ===== 거절(REJECTED) =====
        if ("REJECTED".equals(status)) {
            report.setStatus("REJECTED");
            report.setManagerId(managerId);
            report.setProcessedAt(new Timestamp(System.currentTimeMillis()));

            ReviewReport saved = repo.save(report);
            notification.sendReportProcessed(report.getReporterId(), reportId, "REJECTED");
            return saved;
        }

        // ===== 기타 =====
        report.setStatus(status);
        report.setManagerId(managerId);
        report.setProcessedAt(new Timestamp(System.currentTimeMillis()));

        ReviewReport saved = repo.save(report);
        notification.sendReportProcessed(report.getReporterId(), reportId, status);
        return saved;
    }

    @Transactional
    public void forceDeleteReportedReview(Long reportId, String managerId) {
        int grade = userService.getUserGrade(managerId);
        if (grade != 2) throw new ResponseStatusException(FORBIDDEN, "grade=2만 가능");

        ReviewReport report = repo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "신고 없음"));

        Long reviewId = report.getReviewId();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        repo.updateAllStatusByReviewId(reviewId, "APPROVED", managerId, now);

        reviewCommentLikeRepo.deleteByReviewIdHard(reviewId);
        reviewCommentRepo.deleteByReviewIdHard(reviewId);
        tagRepo.deleteByReviewId(reviewId);

        reviewRepo.softDelete(reviewId);

        notification.sendReportProcessed(report.getReporterId(), reportId, "APPROVED");
    }

    public void delete(Long id) { repo.deleteById(id); }
}
