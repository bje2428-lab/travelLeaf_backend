package dev.jpa.comments_reports;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports/comments")
@RequiredArgsConstructor
public class CommentsReportController {

    private final CommentsReportService service;

    /* ===============================
       신고 등록
    =============================== */
    @PostMapping
    public CommentsReport create(@RequestBody CommentsReportDTO dto) {
        return service.create(dto);
    }

    /* ===============================
       전체 조회
    =============================== */
    @GetMapping
    public List<CommentsReport> findAll() {
        return service.findAll();
    }

    /* ===============================
       단건 조회
    =============================== */
    @GetMapping("/{id}")
    public CommentsReport findOne(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    /* ===============================
       댓글별 조회
    =============================== */
    @GetMapping("/comment/{commentId}")
    public List<CommentsReport> findByComment(@PathVariable("commentId") Long commentId) {
        return service.findByComment(commentId);
    }

    /* ===============================
       신고자별 조회
    =============================== */
    @GetMapping("/reporter/{reporterId}")
    public List<CommentsReport> findByReporter(@PathVariable("reporterId") String reporterId) {
        return service.findByReporter(reporterId);
    }

    /* ===============================
       🔥 관리자 검색 + 페이징 (DTO 반환)
       (DB 커넥션 누수 방지 핵심)
    =============================== */
    @GetMapping("/search")
    public Page<CommentsReportDTO> search(
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return service.searchReports(status, keyword, page, size);
    }

    /* ===============================
       관리자 상태 변경
    =============================== */
    @PutMapping("/{reportId}/status")
    public CommentsReport updateStatus(
            @PathVariable("reportId") Long reportId,
            @RequestParam("status") String status,
            @RequestParam("adminId") String adminId
    ) {
        return service.updateStatus(reportId, status, adminId);
    }

    /* ===============================
       관리자 강제 댓글 삭제
    =============================== */
    @DeleteMapping("/{reportId}/force-delete")
    public void forceDeleteReportedComment(
            @PathVariable("reportId") Long reportId,
            @RequestParam("adminId") String adminId
    ) {
        service.deleteReportedComment(reportId, adminId);
    }

    /* ===============================
       신고 삭제
    =============================== */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
