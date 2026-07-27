package dev.jpa.posts_embeddings;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/embeddings")
@RequiredArgsConstructor
public class PostsEmbeddingController {

    private final PostsEmbeddingService service;

    // ===============================
    // INSERT
    // ===============================
    @PostMapping("/{postsId}")
    public String save(@PathVariable("postsId") Long postsId,
                       @RequestBody PostsEmbeddingDTO dto) {
        service.saveEmbedding(postsId, dto.getEmbedding());
        return "saved";
    }

    // ===============================
    // UPDATE
    // ===============================
    @PutMapping("/{postsId}")
    public String update(@PathVariable("postsId") Long postsId,
                         @RequestBody PostsEmbeddingDTO dto) {
        service.updateEmbedding(postsId, dto.getEmbedding());
        return "updated";
    }

    // ===============================
    // GET
    // ===============================
    @GetMapping("/{postsId}")
    public PostsEmbedding get(@PathVariable("postsId") Long postsId) {
        return service.getEmbedding(postsId);
    }

    // ===============================
    // DELETE
    // ===============================
    @DeleteMapping("/{postsId}")
    public String delete(@PathVariable("postsId") Long postsId) {
        service.deleteEmbedding(postsId);
        return "deleted";
    }

    // ===============================
    // 🔥 전체 게시글 임베딩 재생성
    // ===============================
    @PostMapping("/rebuild")
    public String rebuildAll() {
        service.rebuildAllEmbeddings();
        return "ALL POSTS EMBEDDINGS REBUILT";
    }
}
