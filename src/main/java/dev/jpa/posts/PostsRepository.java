package dev.jpa.posts;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface PostsRepository extends JpaRepository<Posts, Long> {

  /** 전체 목록 */
  List<Posts> findByCatenoOrderByPostIdDesc(long cateno);

  /** 정렬 없는 버전 (Pageable 정렬만 적용) */
  Page<Posts> findByCateno(long cateno, Pageable pageable);

  /** 검색 (항상 최신순) ⭐ favorite_cnt 추가 반영 */
  @Query(value = """
      SELECT post_id, user_id, cateno, title, content,
             recom, favorite_cnt, cnt, replycnt,
             password, word, rdate,
             map, youtube, mp4, visible,
             file1, file1saved, thumb1, size1,
             price, dc, saleprice, point, salecnt
      FROM posts
      WHERE cateno = :cateno
        AND (title LIKE %:word% OR content LIKE %:word% OR word LIKE %:word%)
      ORDER BY post_id DESC
      """, nativeQuery = true)
  Page<Posts> list_all_paging_search(@Param("cateno") long cateno,
                                     @Param("word") String word,
                                     Pageable pageable);


  /** 조회수 증가 */
  @Modifying
  @Transactional
  @Query(value = "UPDATE posts SET cnt = cnt + 1 WHERE post_id = :postId", nativeQuery = true)
  int increaseCnt(@Param("postId") long postId);

  /** 게시글 조회 */
  Posts findByPostId(long postId);


  /** 글 수정 */
  @Modifying
  @Transactional
  @Query(value = """
      UPDATE posts
      SET title = :title,
          content = :content,
          word = :word
      WHERE post_id = :postId
      """, nativeQuery = true)
  int update_text(@Param("title") String title,
                  @Param("content") String content,
                  @Param("word") String word,
                  @Param("postId") long postId);

  /** 관리자 글 수정 */
  @Modifying
  @Query(value="UPDATE posts SET title=:title, content=:content, word=:word WHERE post_id=:postId", nativeQuery = true)
  int update_text_admin(@Param("title") String title,
                        @Param("content") String content,
                        @Param("word") String word,
                        @Param("postId") long postId);


  /** 파일 수정 */
  @Modifying
  @Transactional
  @Query(value = """
      UPDATE posts
      SET file1 = :file1,
          file1saved = :file1saved,
          thumb1 = :thumb1,
          size1 = :size1
      WHERE post_id = :postId
      """, nativeQuery = true)
  int update_file1(@Param("file1") String file1,
                   @Param("file1saved") String file1saved,
                   @Param("thumb1") String thumb1,
                   @Param("size1") long size1,
                   @Param("postId") long postId);


  /** ⭐ 관리자 파일 수정 (추가됨) */
  @Modifying
  @Transactional
  @Query(value = """
      UPDATE posts
      SET file1 = :file1,
          file1saved = :file1saved,
          thumb1 = :thumb1,
          size1 = :size1
      WHERE post_id = :postId
      """, nativeQuery = true)
  int update_file1_admin(@Param("file1") String file1,
                         @Param("file1saved") String file1saved,
                         @Param("thumb1") String thumb1,
                         @Param("size1") long size1,
                         @Param("postId") long postId);


  /** 패스워드 검사 */
  @Query(value = """
      SELECT COUNT(*)
      FROM posts
      WHERE post_id = :postId
        AND password = :password
      """, nativeQuery = true)
  int password_check(@Param("postId") long postId,
                     @Param("password") String password);


  /** 작성자 삭제 */
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM posts WHERE post_id = :postId AND password = :password", nativeQuery = true)
  int delete(@Param("postId") long postId,
             @Param("password") String password);


  /* ===============================
       Youtube
   =============================== */
  @Modifying
  @Transactional
  @Query("""
      UPDATE Posts p
      SET p.youtube = :youtube
      WHERE p.postId = :postId
        AND p.password = :password
      """)
  int youtube(@Param("youtube") String youtube,
              @Param("postId") long postId,
              @Param("password") String password);

  @Modifying
  @Transactional
  @Query("""
      UPDATE Posts p
      SET p.youtube = :youtube
      WHERE p.postId = :postId
      """)
  int youtubeAdmin(@Param("youtube") String youtube,
                   @Param("postId") long postId);


  /* ===============================
       Map
   =============================== */
  @Modifying
  @Transactional
  @Query("""
      UPDATE Posts p
      SET p.map = :map
      WHERE p.postId = :postId
        AND p.password = :password
      """)
  int map(@Param("map") String map,
          @Param("postId") long postId,
          @Param("password") String password);

  @Modifying
  @Transactional
  @Query("""
      UPDATE Posts p
      SET p.map = :map
      WHERE p.postId = :postId
      """)
  int mapAdmin(@Param("map") String map,
               @Param("postId") long postId);


  /* ===============================
      ❤️ 좋아요 업데이트
  =============================== */
  @Modifying
  @Transactional
  @Query("UPDATE Posts p SET p.recom = :recom WHERE p.postId = :postId")
  int updateRecom(@Param("postId") long postId,
                  @Param("recom") long recom);


  /* ===============================
      ⭐ 즐겨찾기 업데이트
  =============================== */
  @Modifying
  @Transactional
  @Query("UPDATE Posts p SET p.favoriteCnt = :favoriteCnt WHERE p.postId = :postId")
  int updateFavoriteCnt(@Param("postId") long postId,
                        @Param("favoriteCnt") long favoriteCnt);


  /* ===============================
      태그 검색
  =============================== */
  @Query("""
   SELECT p
   FROM Posts p
   WHERE p.postId IN (
       SELECT DISTINCT pt.post.postId
       FROM PostTag pt
       JOIN pt.tag t
       WHERE t.name IN :tags
   )
""")
  Page<Posts> findByTagsOr(
       @Param("tags") List<String> tags,
       Pageable pageable
  );


  @Query("""
     SELECT p
     FROM Posts p
     WHERE p.postId IN (
         SELECT pt.post.postId
         FROM PostTag pt
         JOIN pt.tag t
         WHERE t.name IN :tags
         GROUP BY pt.post.postId
         HAVING COUNT(DISTINCT t.name) = :size
     )
 """)
 Page<Posts> findByTagsAnd(
         @Param("tags") List<String> tags,
         @Param("size") long size,
         Pageable pageable
 );


  /** ⭐ 같은 카테고리 추천용: postId → cateno 조회 */
  @Query("SELECT p.cateno FROM Posts p WHERE p.postId = :postId")
  Long findCatenoByPostId(@Param("postId") long postId);
  

  @Query("SELECT p FROM Posts p WHERE p.postId IN :ids")
  List<Posts> findByPostIdIn(@Param("ids") List<Long> ids);


}
