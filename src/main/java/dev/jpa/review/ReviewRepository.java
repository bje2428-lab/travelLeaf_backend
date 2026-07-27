package dev.jpa.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ Hibernate가 찾는 컬럼 라벨이 소문자(snake)로 떨어지는 경우가 있어
    //    nativeQuery에서 alias를 강제로 맞춰 ORA-17006을 막는다.
    String REVIEW_COLS =
            "REVIEW_ID   AS review_id, " +
            "CITY        AS city, " +
            "DISTRICT    AS district, " +
            "PLACE_NAME  AS place_name, " +
            "USER_ID     AS user_id, " +
            "RATING      AS rating, " +
            "CONTENT     AS content, " +
            "CREATED_AT  AS created_at, " +
            "UPDATED_AT  AS updated_at, " +
            "TOXIC_SCORE AS toxic_score, " +
            "FLAG_REASON AS flag_reason, " +
            "SENTIMENT   AS sentiment, " +
            "MODERATED_AT AS moderated_at, " +
            "AI_SUMMARY  AS ai_summary, " +
            "AI_KEYWORDS AS ai_keywords, " +
            "AI_SUMMARY_AT AS ai_summary_at, " +
            "IS_DELETED  AS is_deleted, " +
            "DELETED_AT  AS deleted_at";

    String NOT_DELETED = "NVL(IS_DELETED, 0) = 0";

    String Q_FIND_BY_CITY_DISTRICT =
            "SELECT " + REVIEW_COLS + " " +
            "FROM REVIEW " +
            "WHERE " + NOT_DELETED + " " +
            "  AND TRIM(REPLACE(CITY, ' ', '')) = TRIM(REPLACE(:city, ' ', '')) " +
            "  AND TRIM(REPLACE(DISTRICT, ' ', '')) = TRIM(REPLACE(:district, ' ', '')) " +
            "ORDER BY CREATED_AT DESC";

    String Q_SEARCH =
            "SELECT " + REVIEW_COLS + " " +
            "FROM REVIEW " +
            "WHERE " + NOT_DELETED + " " +
            "  AND TRIM(REPLACE(CITY, ' ', '')) = TRIM(REPLACE(:city, ' ', '')) " +
            "  AND TRIM(REPLACE(DISTRICT, ' ', '')) = TRIM(REPLACE(:district, ' ', '')) " +
            "  AND ( :keyword IS NULL OR TRIM(:keyword) = '' OR PLACE_NAME LIKE '%' || :keyword || '%' ) " +
            "ORDER BY " +
            "  CASE WHEN :sortBy = 'createdAt' AND :direction = 'asc'  THEN CREATED_AT END ASC, " +
            "  CASE WHEN :sortBy = 'createdAt' AND :direction = 'desc' THEN CREATED_AT END DESC, " +
            "  CASE WHEN :sortBy = 'rating' AND :direction = 'asc'  THEN RATING END ASC, " +
            "  CASE WHEN :sortBy = 'rating' AND :direction = 'desc' THEN RATING END DESC, " +
            "  CREATED_AT DESC";

    String Q_SEARCH_COUNT =
            "SELECT COUNT(*) " +
            "FROM REVIEW " +
            "WHERE " + NOT_DELETED + " " +
            "  AND TRIM(REPLACE(CITY, ' ', '')) = TRIM(REPLACE(:city, ' ', '')) " +
            "  AND TRIM(REPLACE(DISTRICT, ' ', '')) = TRIM(REPLACE(:district, ' ', '')) " +
            "  AND ( :keyword IS NULL OR TRIM(:keyword) = '' OR PLACE_NAME LIKE '%' || :keyword || '%' )";

    @Query(value = Q_FIND_BY_CITY_DISTRICT, nativeQuery = true)
    List<Review> findByCityAndDistrictNative(
            @Param("city") String city,
            @Param("district") String district
    );

    @Query(value = Q_SEARCH, countQuery = Q_SEARCH_COUNT, nativeQuery = true)
    Page<Review> searchByCityDistrictWithPaging(
            @Param("city") String city,
            @Param("district") String district,
            @Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            @Param("direction") String direction,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE REVIEW " +
                    "SET IS_DELETED = 1, DELETED_AT = SYSTIMESTAMP " +
                    "WHERE REVIEW_ID = :reviewId",
            nativeQuery = true
    )
    int softDelete(@Param("reviewId") Long reviewId);

    @Query("SELECT r FROM Review r " +
           "WHERE r.reviewId = :reviewId " +
           "  AND r.userId = :userId " +
           "  AND (r.isDeleted = 0 OR r.isDeleted IS NULL)")
    Review findByReviewIdAndUserId(
            @Param("reviewId") Long reviewId,
            @Param("userId") String userId
    );
}
