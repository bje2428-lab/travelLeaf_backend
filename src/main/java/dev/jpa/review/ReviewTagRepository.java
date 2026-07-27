package dev.jpa.review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, ReviewTagId> {

    // 태그 조회
    List<ReviewTag> findByReviewId(Long reviewId);

    // 태그 삭제(리뷰 삭제/재생성 시 사용)
    @Modifying
    @Transactional
    @Query("delete from ReviewTag t where t.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);
}
