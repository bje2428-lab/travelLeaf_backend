package dev.jpa.posts_embeddings;

import dev.jpa.posts.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostsEmbeddingRepository extends JpaRepository<PostsEmbedding, Long> {

    /** 
     * ⭐ 같은 카테고리 게시글 임베딩만 조회 
     *  - POSTS_EMBEDDINGS e
     *  - POSTS p JOIN
     *  - p.cateno = :cateno
     */
    @Query("""
        SELECT e
        FROM PostsEmbedding e
        JOIN Posts p
          ON e.postsId = p.postId
        WHERE p.cateno = :cateno
    """)
    List<PostsEmbedding> findByCategory(@Param("cateno") long cateno);
}
