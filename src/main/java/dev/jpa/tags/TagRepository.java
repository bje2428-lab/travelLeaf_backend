package dev.jpa.tags;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jpa.posts.Posts;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
Optional<Tag> findByName(String name);
// 🔥 자동완성용
List<Tag> findByNameContainingIgnoreCase(String keyword);
boolean existsByName(String name);

@Query("""
    SELECT t, COUNT(pt) AS cnt
    FROM PostTag pt
    JOIN pt.tag t
    GROUP BY t
    ORDER BY COUNT(pt) DESC
""")
List<Object[]> findPopularTags(Pageable pageable);

/**
 * OR 검색: 하나라도 포함
 */
@Query("""
    SELECT DISTINCT p
    FROM Posts p
    JOIN PostTag pt ON pt.post.postId = p.postId
    JOIN pt.tag t
    WHERE t.name IN :tags
""")
Page<Posts> findByTagsOr(
        @Param("tags") List<String> tags,
        Pageable pageable
);

/**
 * AND 검색: 모두 포함
 */
@Query("""
    SELECT p
    FROM Posts p
    JOIN PostTag pt ON pt.post.postId = p.postId
    JOIN pt.tag t
    WHERE t.name IN :tags
    GROUP BY p
    HAVING COUNT(DISTINCT t.name) = :size
""")
Page<Posts> findByTagsAnd(
        @Param("tags") List<String> tags,
        @Param("size") long size,
        Pageable pageable
);
}