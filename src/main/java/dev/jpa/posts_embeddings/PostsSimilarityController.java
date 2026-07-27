package dev.jpa.posts_embeddings;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsSimilarityController {

    private final PostsSimilarityService service;

    @GetMapping("/{postId}/similar")
    public List<SimilarPostDTO> similar(@PathVariable("postId") long postId) {
        return service.findSimilarPosts(postId);
    }

}
