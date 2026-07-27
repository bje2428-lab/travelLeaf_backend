package dev.jpa.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    Page<ReviewComment> findByReviewIdOrderByCreatedAtAsc(Long reviewId, Pageable pageable);

    @Query("select c.commentId from ReviewComment c where c.reviewId = :reviewId")
    List<Long> findCommentIdsByReviewId(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("delete from ReviewComment c where c.reviewId = :reviewId")
    void deleteByReviewIdHard(@Param("reviewId") Long reviewId);
}
