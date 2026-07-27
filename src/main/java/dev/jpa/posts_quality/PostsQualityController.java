package dev.jpa.posts_quality;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/quality")
@RequiredArgsConstructor
public class PostsQualityController {

    private final PostsQualityService service;

    // 🔥 글 하나 AI 분석 실행
    @PostMapping("/{postId}")
    public ResponseEntity<Void> analyze(@PathVariable("postId") Long postId) {
        service.analyzePost(postId);
        return ResponseEntity.ok().build();
    }

    // 🔥 AI 점수 조회 (없으면 204 No Content 반환)
    @GetMapping("/{postId}")
    public ResponseEntity<QualityScoreDTO> get(@PathVariable("postId") Long postId) {

        QualityScoreDTO dto = service.getScore(postId);

        // ✅ 아직 AI 분석이 안 된 게시글
        if (dto == null) {
            return ResponseEntity.noContent().build();   // HTTP 204
        }

        // ✅ AI 점수 존재
        return ResponseEntity.ok(dto);
    }
}
