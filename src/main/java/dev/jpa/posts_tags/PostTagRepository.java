package dev.jpa.posts_tags;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import dev.jpa.posts.Posts;

public interface PostTagRepository
        extends JpaRepository<PostTag, PostTagId> {

//🔥 게시글 기준 태그 매핑 전체 삭제
  void deleteByPost_PostId(long postId);
  
    /* =========================
       게시글 → 태그 조회 (기존)
    ========================= */
    @Query("""
        SELECT pt
        FROM PostTag pt
        JOIN FETCH pt.tag
        WHERE pt.post.postId = :postId
    """)
    List<PostTag> findByPostIdWithTag(@Param("postId") long postId);

    List<PostTag> findByTag_TagId(long tagId);

    /* =========================
       🔥 태그 → 게시글 조회 (신규)
    ========================= */
    @Query("""
        SELECT p
        FROM Posts p
        JOIN PostTag pt ON pt.post.postId = p.postId
        WHERE pt.tag.tagId = :tagId
        ORDER BY p.postId DESC
    """)
    Page<Posts> findPostsByTagId(
            @Param("tagId") long tagId,
            Pageable pageable
    );
}
