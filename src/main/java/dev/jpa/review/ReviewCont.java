package dev.jpa.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewCont {

    @Autowired
    private ReviewService service;

    /* ===========================
       기본 CRUD
       =========================== */

    @PostMapping("/create")
    public ResponseEntity<Review> create(@RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Review>> list(
            @RequestParam("city") String city,
            @RequestParam("district") String district
    ) {
        return ResponseEntity.ok(service.list(city, district));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Review>> search(
            @RequestParam("city") String city,
            @RequestParam("district") String district,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(service.search(city, district, keyword, page, size, sortBy, direction));
    }

    @GetMapping("/detail/{reviewId}")
    public ResponseEntity<Review> detail(@PathVariable("reviewId") Long reviewId) {
        return ResponseEntity.ok(service.detail(reviewId));
    }

    // ✅ 리뷰 상세 + 댓글까지 한번에
    // 예) /review/detail-with-comments/12?userId=aaa&cpage=0&csize=20
    @GetMapping("/detail-with-comments/{reviewId}")
    public ResponseEntity<ReviewDetailResponseDTO> detailWithComments(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "cpage", defaultValue = "0") int cpage,
            @RequestParam(value = "csize", defaultValue = "20") int csize
    ) {
        return ResponseEntity.ok(service.detailWithComments(reviewId, userId, cpage, csize));
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<Review> update(
            @PathVariable("reviewId") Long reviewId,
            @RequestBody ReviewDTO dto
    ) {
        return ResponseEntity.ok(service.update(reviewId, dto));
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<String> delete(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam("userId") String userId
    ) {
        service.delete(reviewId, userId);
        return ResponseEntity.ok("deleted");
    }

    /* ===========================
       ✅ AI 요약/태그 API
       =========================== */

    // ✅ 1줄 요약 + 키워드 + 태그 생성(버튼용)
    @GetMapping("/ai/summary/{reviewId}")
    public ResponseEntity<AiReviewSummaryResponse> aiSummary(
            @PathVariable("reviewId") Long reviewId
    ) {
        return ResponseEntity.ok(service.generateSummary(reviewId));
    }

    // ✅ 태그 조회 (없으면 빈 리스트)
    @GetMapping("/ai/tags/{reviewId}")
    public ResponseEntity<List<ReviewTag>> getTags(
            @PathVariable("reviewId") Long reviewId
    ) {
        return ResponseEntity.ok(service.getTags(reviewId));
    }

    // ✅ 태그 재생성
    @PostMapping("/ai/tags/regenerate/{reviewId}")
    public ResponseEntity<List<ReviewTag>> regenTags(
            @PathVariable("reviewId") Long reviewId
    ) {
        return ResponseEntity.ok(service.regenerateTags(reviewId));
    }

    /* ===========================
       ✅ 번역 API (저장 X, 화면 표시용)
       =========================== */

    // ✅ 번역: reviewId 기준으로 placeName/content 번역해서 반환 (DB 저장 안 함)
    // 예) /review/ai/translate/12?targetLang=en
    @GetMapping("/ai/translate/{reviewId}")
    public ResponseEntity<ReviewAiService.TranslateReviewResponse> translate(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam(value = "targetLang", defaultValue = "en") String targetLang
    ) {
        return ResponseEntity.ok(service.translateReview(reviewId, targetLang));
    }
}
