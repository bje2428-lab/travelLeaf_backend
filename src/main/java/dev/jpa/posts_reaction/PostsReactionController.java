package dev.jpa.posts_reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class PostsReactionController {

    private final PostsReactionService service;

    /* ===============================
       CREATE
       POST /reactions
    =============================== */
    @PostMapping
    public PostsReaction create(@RequestBody PostsReactionDTO dto) {
        return service.create(dto);
    }

    /* ===============================
       READ ONE
       GET /reactions/{id}
    =============================== */
    @GetMapping("/{id}")
    public PostsReaction findOne(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    /* ===============================
       DELETE
       DELETE /reactions/{id}
    =============================== */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }

    /* ===============================
       ❤️ 특정 유저 + 타입
       GET /reactions/user/{userId}/type/{type}?page=0&size=10
    =============================== */
    @GetMapping("/user/{userId}/type/{type}")
    public Page<PostsReaction> userType(
            @PathVariable("userId") String userId,
            @PathVariable("type") String type,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        return service.findByUserAndType(userId, type, PageRequest.of(page, size));
    }

    /* ===============================
       📌 특정 게시글 + 타입
       GET /reactions/post/{postId}/type/{type}?page=0&size=10
    =============================== */
    @GetMapping("/post/{postId}/type/{type}")
    public Page<PostsReaction> postType(
            @PathVariable("postId") Long postId,
            @PathVariable("type") String type,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        return service.findByPost(postId, type, PageRequest.of(page, size));
    }
}
