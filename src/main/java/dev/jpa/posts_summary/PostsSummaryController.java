package dev.jpa.posts_summary;

import dev.jpa.ai.summary.PostsSummaryAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/summary")
@RequiredArgsConstructor
public class PostsSummaryController {

    private final PostsSummaryAiService aiService;
    private final PostsSummaryService service;

    @GetMapping("/{postId}")
    public PostsSummaryDTO get(@PathVariable Long postId) {
        return service.getByPostId(postId);
    }

    @PostMapping("/ai/{postId}")
    public void generate(@PathVariable Long postId) {
        aiService.generateSummary(postId);
    }

    @PostMapping
    public void save(@RequestBody PostsSummaryDTO dto) {
        service.save(dto);
    }
}
