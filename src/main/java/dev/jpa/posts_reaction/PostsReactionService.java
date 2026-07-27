package dev.jpa.posts_reaction;

import dev.jpa.posts.Posts;
import dev.jpa.posts.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostsReactionService {

    private final PostsReactionRepository repo;
    private final PostsRepository postsRepository;

    /* ======================================================
       CREATE
    ====================================================== */
    public PostsReaction create(PostsReactionDTO dto) {

        if (!postsRepository.existsById(dto.getPostId())) {
            throw new RuntimeException("존재하지 않는 게시글입니다.");
        }

        if (repo.existsByUserIdAndPostIdAndType(
                dto.getUserId(),
                dto.getPostId(),
                dto.getType())
        ) {
            throw new RuntimeException("이미 해당 반응이 존재합니다.");
        }

        PostsReaction r = PostsReaction.builder()
                .reactionId(dto.getReactionId())
                .userId(dto.getUserId())
                .postId(dto.getPostId())
                .type(dto.getType())
                .createdAt(new java.util.Date())
                .build();

        PostsReaction saved = repo.save(r);
        updatePostCounts(dto.getPostId());

        return saved;
    }

    /* ======================================================
       DELETE
    ====================================================== */
    public void delete(Long id) {

        PostsReaction reaction = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 반응입니다."));

        repo.deleteById(id);

        if (postsRepository.existsById(reaction.getPostId())) {
            updatePostCounts(reaction.getPostId());
        }
    }

    /* ======================================================
       ❤️ & ⭐ 동기화
    ====================================================== */
    private void updatePostCounts(Long postId) {

        Posts post = postsRepository.findById(postId).orElse(null);
        if (post == null) return;

        long likeCount = repo.countValidByPostIdAndType(postId, "like");
        long favCount  = repo.countValidByPostIdAndType(postId, "favorite");

        post.setRecom((int) likeCount);
        post.setFavoriteCnt((int) favCount);

        postsRepository.save(post);
    }

    /* ======================================================
       JOIN 기반 조회
       컨트롤러와 이름 정확히 일치
    ====================================================== */

    // GET /reactions/user/{userId}/type/{type}
    public Page<PostsReaction> findByUserAndType(
            String userId,
            String type,
            Pageable pageable
    ) {
        return repo.findValidReactions(userId, type, pageable);
    }

    // GET /reactions/post/{postId}/type/{type}
    public Page<PostsReaction> findByPost(
            Long postId,
            String type,
            Pageable pageable
    ) {
        return repo.findValidByPostIdAndType(postId, type, pageable);
    }

    /* ======================================================
       단건
    ====================================================== */
    public PostsReaction findOne(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 반응입니다."));
    }

    /* ======================================================
       UPDATE
    ====================================================== */
    public PostsReaction update(String reactionId, String type) {

        PostsReaction r = repo.findByReactionId(reactionId);
        if (r == null) throw new RuntimeException("Reaction not found");

        if (!postsRepository.existsById(r.getPostId())) {
            throw new RuntimeException("게시글이 삭제되었습니다.");
        }

        if (repo.existsByUserIdAndPostIdAndType(
                r.getUserId(),
                r.getPostId(),
                type)
        ) {
            throw new RuntimeException("이미 해당 타입 반응이 존재합니다.");
        }

        r.setType(type);
        PostsReaction saved = repo.save(r);

        updatePostCounts(r.getPostId());

        return saved;
    }
}
