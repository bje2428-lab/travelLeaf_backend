package dev.jpa.comments;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentsRepository extends JpaRepository<Comments, Long> {

    /* ===============================
       기존 유지
    =============================== */

    List<Comments> findByPostIdAndIsDeletedOrderByCommentIdAsc(
            Long postId,
            String isDeleted
    );

    List<Comments> findByParentCommentId(Long parentCommentId);

    Comments findByCommentIdAndIsDeleted(Long commentId, String isDeleted);


    /* =========================================================
       ✅ 기존 트리 (기본: 작성순 정렬)
    ========================================================= */
    @Query("""
        SELECT c FROM Comments c
        WHERE c.postId = :postId
          AND c.isDeleted = 'N'
        ORDER BY c.parentCommentId NULLS FIRST, c.commentId ASC
    """)
    List<Comments> findTreeByPostId(@Param("postId") Long postId);


    // ✅ 기존 페이징
    Page<Comments> findByPostIdAndIsDeleted(
            Long postId,
            String isDeleted,
            Pageable pageable
    );


    /* ===============================
       추가된 기능
    =============================== */

    // 🔥 댓글 수 API
    long countByPostIdAndIsDeleted(Long postId, String isDeleted);


    // 🔥 최신 / 오래된 순 정렬용
    List<Comments> findByPostIdAndIsDeleted(
            Long postId,
            String isDeleted,
            Sort sort
    );


    /* =========================================================
       🔥🔥 좋아요 많은 댓글 정렬 (핵심)
       ---------------------------------------------------------
       COMMENT_REACTIONS 와 LEFT JOIN 후
       COUNT() 로 좋아요 수 집계 → 내림차순 정렬
    ========================================================= */
    @Query("""
        SELECT c FROM Comments c
        LEFT JOIN CommentReaction cr 
            ON cr.commentId = c.commentId
        WHERE c.postId = :postId
          AND c.isDeleted = 'N'
        GROUP BY c
        ORDER BY COUNT(cr) DESC, c.createdAt DESC
    """)
    List<Comments> findByPostIdOrderByLikesDesc(@Param("postId") Long postId);



    /* ===============================
       Soft delete
    =============================== */

    @Modifying
    @Transactional
    @Query("UPDATE Comments c SET c.isDeleted = 'Y' WHERE c.commentId = :commentId")
    void softDeleteById(@Param("commentId") Long commentId);

    @Modifying
    @Transactional
    @Query("UPDATE Comments c SET c.isDeleted = 'Y' WHERE c.parentCommentId = :parentCommentId")
    void softDeleteRepliesByParentCommentId(
            @Param("parentCommentId") Long parentCommentId
    );

    @Modifying
    @Transactional
    @Query("UPDATE Comments c SET c.isDeleted = 'Y' WHERE c.postId = :postId")
    void softDeleteAllByPostId(@Param("postId") Long postId);
    
    @Query("SELECT c.commentId FROM Comments c WHERE c.postId = :postId")
    List<Long> findCommentIdsByPostId(@Param("postId") Long postId);
    
 // 🔥 특정 댓글 + 그에 딸린 대댓글 ID 전부 조회
    @Query("""
        SELECT c.commentId
        FROM Comments c
        WHERE c.commentId = :rootId
           OR c.parentCommentId = :rootId
    """)
    List<Long> findThreadCommentIds(@Param("rootId") Long rootId);


}
