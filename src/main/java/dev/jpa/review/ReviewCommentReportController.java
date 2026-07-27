package dev.jpa.review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports/review-comments")
@RequiredArgsConstructor
public class ReviewCommentReportController {

    private final ReviewCommentReportService service;

    @PostMapping
    public ReviewCommentReport create(@RequestBody ReviewCommentReportDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<ReviewCommentReport> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ReviewCommentReport findOne(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    @GetMapping("/comment/{commentId}")
    public List<ReviewCommentReport> findByComment(@PathVariable("commentId") Long commentId) {
        return service.findByComment(commentId);
    }

    @GetMapping("/reporter/{reporterId}")
    public List<ReviewCommentReport> findByReporter(@PathVariable("reporterId") String reporterId) {
        return service.findByReporter(reporterId);
    }

    @GetMapping("/search")
    public Page<ReviewCommentReportDTO> search(
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return service.searchReports(status, keyword, page, size);
    }

    @PutMapping("/{reportId}/status")
    public ReviewCommentReport updateStatus(
            @PathVariable("reportId") Long reportId,
            @RequestParam("status") String status,
            @RequestParam("managerId") String managerId
    ) {
        return service.updateStatus(reportId, status, managerId);
    }

    @DeleteMapping("/{reportId}/force-delete")
    public void forceDeleteReportedComment(
            @PathVariable("reportId") Long reportId,
            @RequestParam("managerId") String managerId
    ) {
        service.forceDeleteReportedComment(reportId, managerId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
