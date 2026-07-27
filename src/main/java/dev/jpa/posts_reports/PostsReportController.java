package dev.jpa.posts_reports;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports/posts")
@RequiredArgsConstructor
public class PostsReportController {

    private final PostsReportService service;

    @PostMapping
    public PostsReport create(@RequestBody PostsReportDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<PostsReport> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PostsReport findOne(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    @GetMapping("/post/{postId}")
    public List<PostsReport> findByPost(@PathVariable("postId") Long postId) {
        return service.findByPost(postId);
    }

    @GetMapping("/reporter/{reporterId}")
    public List<PostsReport> findByReporter(@PathVariable("reporterId") String reporterId) {
        return service.findByReporter(reporterId);
    }

    @GetMapping("/search")
    public Page<PostsReportDTO> search(
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return service.searchReports(status, keyword, page, size);
    }


    @PutMapping("/{reportId}/status")
    public PostsReport updateStatus(
            @PathVariable("reportId") Long reportId,
            @RequestParam("status") String status,
            @RequestParam("adminId") String adminId
    ) {
        return service.updateStatus(reportId, status, adminId);
    }

    @DeleteMapping("/{reportId}/force-delete")
    public void forceDeleteReportedPost(
            @PathVariable("reportId") Long reportId,
            @RequestParam("adminId") String adminId
    ) {
        service.deleteReportedPost(reportId, adminId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
