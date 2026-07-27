package dev.jpa.posts_embeddings;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.posts.Posts;

@Service
@RequiredArgsConstructor
public class PostsEmbeddingService {

    private final PostsEmbeddingRepository repo;
    private final EmbeddingAiClient embeddingAiClient;

    // 🔥 게시글 원본 조회용
    private final dev.jpa.posts.PostsRepository postsRepository;

    /* ===============================
       INSERT  🔥 실제 AI 임베딩 저장
    =============================== */
    @Transactional
    public void saveEmbedding(Long postsId, String text) {

        String embeddingJson = embeddingAiClient.createEmbedding(text);

        PostsEmbedding emb = new PostsEmbedding();
        emb.setPostsId(postsId);
        emb.setEmbedding(embeddingJson);
        emb.setUpdatedAt(LocalDateTime.now());

        repo.save(emb);
    }

    /* ===============================
       UPDATE  🔥 수정 시 재생성
    =============================== */
    @Transactional
    public void updateEmbedding(Long postsId, String text) {

        String embeddingJson = embeddingAiClient.createEmbedding(text);

        PostsEmbedding emb = repo.findById(postsId)
                .orElseThrow(() ->
                        new RuntimeException("Embedding not found for postsId=" + postsId));

        emb.setEmbedding(embeddingJson);
        emb.setUpdatedAt(LocalDateTime.now());

        repo.save(emb);
    }

    /* ===============================
       SELECT
    =============================== */
    @Transactional(readOnly = true)
    public PostsEmbedding getEmbedding(Long postsId) {
        return repo.findById(postsId)
                .orElseThrow(() ->
                        new RuntimeException("Embedding not found for postsId=" + postsId));
    }

    /* ===============================
       DELETE
    =============================== */
    @Transactional
    public void deleteEmbedding(Long postsId) {
        repo.deleteById(postsId);
    }

    /* ===============================
       🔥 전체 게시글 임베딩 재생성 (REBUILD)
    =============================== */
    @Transactional
    public void rebuildAllEmbeddings() {

        List<Posts> postsList = postsRepository.findAll();

        for (Posts post : postsList) {

            String text = post.getTitle() + " " + post.getContent();

            String embeddingJson = embeddingAiClient.createEmbedding(text);

            PostsEmbedding emb = new PostsEmbedding();
            emb.setPostsId(post.getPostId());
            emb.setEmbedding(embeddingJson);
            emb.setUpdatedAt(LocalDateTime.now());

            repo.save(emb);
        }
    }
}
