package dev.jpa.posts_tags;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import dev.jpa.posts.PageResponse;
import dev.jpa.posts.PostsDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post-tags")
public class PostTagController {

    private final PostTagService postTagService;

    /**
     * 게시글에 연결된 태그 조회
     * GET /api/post-tags/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostTagResponseDTO>> getTagsByPost(
            @PathVariable("postId") long postId   // ✅ 이름 명시 (핵심)
    ) {
        return ResponseEntity.ok(
                postTagService.getTagsByPost(postId)
        );
    }

    /**
     * 태그에 속한 게시글 목록 조회 (페이징)
     * GET /api/post-tags/tag/{tagId}/posts?page=0&size=10
     */
    @GetMapping("/tag/{tagId}/posts")
    public ResponseEntity<PageResponse<PostsDTO>> getPostsByTag(
            @PathVariable("tagId") long tagId,    // ✅ 이름 명시
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                postTagService.getPostsByTag(tagId, pageable)
        );
    }
}
