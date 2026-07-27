package dev.jpa.posts_reaction;

import dev.jpa.posts.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostsReactionRepository extends JpaRepository<PostsReaction, Long> {

    /* ============================
       기본 존재 여부
    ============================ */
    boolean existsByUserIdAndPostIdAndType(String userId, Long postId, String type);

    PostsReaction findByReactionId(String reactionId);

    /* ============================
       ❌ 기존 메서드 (삭제된 글 포함됨 → 사용 금지)
       컴파일은 되지만 Service에서 호출하지 않음
    ============================ */
    @Deprecated
    Page<PostsReaction> findByUserId(String userId, Pageable pageable);

    @Deprecated
    Page<PostsReaction> findByPostId(Long postId, Pageable pageable);

    @Deprecated
    Page<PostsReaction> findByType(String type, Pageable pageable);

    @Deprecated
    Page<PostsReaction> findByUserIdAndType(String userId, String type, Pageable pageable);

    /* ============================
       ✅ JOIN 기반 안전 메서드
       → 삭제된 게시글 자동 제거
    ============================ */

    /**
     * 사용자 + 타입(좋아요/즐겨찾기)
     * 삭제되지 않은 게시글만 조회
     */
    @Query("""
        SELECT r
        FROM PostsReaction r
        JOIN Posts p ON r.postId = p.postId
        WHERE r.userId = :userId
          AND r.type = :type
    """)
    Page<PostsReaction> findValidReactions(
        @Param("userId") String userId,
        @Param("type") String type,
        Pageable pageable
    );

    /**
     * 특정 게시글의 좋아요/즐겨찾기 개수
     * (삭제된 게시글이면 0 반환)
     */
    @Query("""
        SELECT COUNT(r)
        FROM PostsReaction r
        JOIN Posts p ON r.postId = p.postId
        WHERE r.postId = :postId
          AND r.type = :type
    """)
    long countValidByPostIdAndType(
        @Param("postId") Long postId,
        @Param("type") String type
    );

    /**
     * 특정 게시글의 반응 목록
     * (삭제된 게시글 자동 제외)
     */
    @Query("""
        SELECT r
        FROM PostsReaction r
        JOIN Posts p ON r.postId = p.postId
        WHERE r.postId = :postId
          AND r.type = :type
    """)
    Page<PostsReaction> findValidByPostIdAndType(
        @Param("postId") Long postId,
        @Param("type") String type,
        Pageable pageable
    );
}
