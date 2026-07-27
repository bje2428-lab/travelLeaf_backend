package dev.jpa.review;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewCommentLikeRepository extends JpaRepository<ReviewCommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, String userId);

    @Modifying
    void deleteByCommentIdAndUserId(Long commentId, String userId);

    long countByCommentId(Long commentId);

    @Query("select l.commentId from ReviewCommentLike l where l.userId = :userId and l.commentId in :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") String userId, @Param("commentIds") List<Long> commentIds);
    
    @Query("select l.commentId, count(l) from ReviewCommentLike l where l.commentId in :ids group by l.commentId")
    List<Object[]> countLikesByCommentIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "delete from REVIEW_COMMENT_LIKE where COMMENT_ID in (select COMMENT_ID from REVIEW_COMMENT where REVIEW_ID = :reviewId)", nativeQuery = true)
    void deleteByReviewIdHard(@Param("reviewId") Long reviewId);

    @Modifying
    @Query(value = "delete from REVIEW_COMMENT_LIKE where COMMENT_ID = :commentId", nativeQuery = true)
    void deleteByCommentIdHard(@Param("commentId") Long commentId);
}
