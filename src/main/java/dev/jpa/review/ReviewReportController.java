package dev.jpa.review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports/reviews")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService service;

    @PostMapping
    public ReviewReport create(@RequestBody ReviewReportDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<ReviewReport> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ReviewReport findOne(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    @GetMapping("/review/{reviewId}")
    public List<ReviewReport> findByReview(@PathVariable("reviewId") Long reviewId) {
        return service.findByReview(reviewId);
    }

    @GetMapping("/reporter/{reporterId}")
    public List<ReviewReport> findByReporter(@PathVariable("reporterId") String reporterId) {
        return service.findByReporter(reporterId);
    }

    @GetMapping("/search")
    public Page<ReviewReportDTO> search(
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return service.searchReports(status, keyword, page, size);
    }

    @PutMapping("/{reportId}/status")
    public ReviewReport updateStatus(
            @PathVariable("reportId") Long reportId,
            @RequestParam("status") String status,
            @RequestParam("managerId") String managerId
    ) {
        return service.updateStatus(reportId, status, managerId);
    }

    @DeleteMapping("/{reportId}/force-delete")
    public void forceDeleteReportedReview(
            @PathVariable("reportId") Long reportId,
            @RequestParam("managerId") String managerId
    ) {
        service.forceDeleteReportedReview(reportId, managerId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
