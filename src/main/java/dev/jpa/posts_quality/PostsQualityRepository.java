package dev.jpa.posts_quality;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface PostsQualityRepository extends JpaRepository<PostsQuality, Long> {

    @Modifying
    @Transactional
    @Query(value = """
    MERGE INTO POSTS_QUALITY q
    USING (
      SELECT
        :postId       AS post_id,
        :readability  AS readability,
        :originality  AS originality,
        :usefulness   AS usefulness,
        :aiScore      AS ai_score,
        :spamScore    AS spam_score,
        :qualityGrade AS quality_grade
      FROM dual
    ) src
    ON (q.post_id = src.post_id)
    WHEN MATCHED THEN
      UPDATE SET
        q.readability   = src.readability,
        q.originality   = src.originality,
        q.usefulness    = src.usefulness,
        q.ai_score      = src.ai_score,
        q.spam_score    = src.spam_score,
        q.quality_grade = src.quality_grade,
        q.analyzed_at   = SYSTIMESTAMP
    WHEN NOT MATCHED THEN
      INSERT (
        post_id,
        readability,
        originality,
        usefulness,
        ai_score,
        spam_score,
        quality_grade,
        analyzed_at
      )
      VALUES (
        src.post_id,
        src.readability,
        src.originality,
        src.usefulness,
        src.ai_score,
        src.spam_score,
        src.quality_grade,
        SYSTIMESTAMP
      )
    """, nativeQuery = true)
    void upsertQuality(
        @Param("postId") Long postId,
        @Param("readability") Double readability,
        @Param("originality") Double originality,
        @Param("usefulness") Double usefulness,
        @Param("aiScore") Double aiScore,
        @Param("spamScore") Double spamScore,
        @Param("qualityGrade") String qualityGrade
    );
}
