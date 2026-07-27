package dev.jpa.posts;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.ai.moderation.ModerationService;   // 🔥 추가
import dev.jpa.comments.CommentsRepository;
import dev.jpa.comments_reports.CommentsReportRepository;
import dev.jpa.posts_embeddings.PostsEmbeddingService;
import dev.jpa.posts_tags.PostTagService;
import dev.jpa.tool.Tool;
import dev.jpa.user.UserService;

@Service
@Transactional
public class PostsService {

  @Autowired
  private PostsRepository postsRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private PostTagService postTagService;
  
  @Autowired
  private CommentsReportRepository commentsReportRepository;

  @Autowired
  private CommentsRepository commentsRepository;


  // 🔥 임베딩 서비스
  @Autowired
  private PostsEmbeddingService postsEmbeddingService;

  // 🔥 LLM 악성도 분석 서비스
  @Autowired
  private ModerationService moderationService;

  /* ===============================
     목록 / 조회
  =============================== */

  public List<Posts> findByCatenoOrderByPostIdDesc(long cateno) {
    return postsRepository.findByCatenoOrderByPostIdDesc(cateno);
  }

  public Page<Posts> findByCateno(long cateno, Pageable pageable) {
    return postsRepository.findByCateno(cateno, pageable);
  }

  public Page<Posts> list_all_paging_search(long cateno, String word, Pageable pageable) {
    return postsRepository.list_all_paging_search(cateno, word, pageable);
  }

  /* ===============================
     게시글 저장 (+ 태그 + AI 임베딩 + 🔥 AI 악성도 분석)
  =============================== */

  public Posts save(PostsDTO postsDTO) {
    postsDTO.setRdate(Tool.getDate());

    Posts posts = postsDTO.toEntityWithFile();

    if (posts.getFile1() == null || posts.getFile1().isEmpty())
      posts.setFile1("");

    if (posts.getFile1saved() == null || posts.getFile1saved().isEmpty())
      posts.setFile1saved("none1.png");

    if (posts.getThumb1() == null || posts.getThumb1().isEmpty())
      posts.setThumb1("none1.png");

    // 🔥 게시글 저장
    Posts savedPost = postsRepository.save(posts);

    // 🔥 태그 저장
    postTagService.replaceTags(
        savedPost.getPostId(),
        postsDTO.getTags()
    );

    // 🔥 LLM 악성도 분석 (실패해도 게시글 등록 유지)
    try {
      moderationService.analyzePost(
          savedPost.getPostId(),
          savedPost.getContent()
      );
    } catch (Exception e) {
      System.out.println("⚠ AI 악성도 분석 실패 (게시글 등록은 유지) postId=" + savedPost.getPostId());
      e.printStackTrace();
    }

    // 🔥 AI 임베딩 생성 (부가 기능)
    try {
      postsEmbeddingService.saveEmbedding(
          savedPost.getPostId(),
          (savedPost.getTitle() == null ? "" : savedPost.getTitle()) + " "
          + (savedPost.getContent() == null ? "" : savedPost.getContent())
      );
    } catch (Exception e) {
      System.out.println("⚠ 임베딩 생성 실패 (게시글 등록은 유지) postId=" + savedPost.getPostId());
      e.printStackTrace();
    }

    return savedPost;
  }

  @Transactional
  public void increaseCnt(Long postId) {
      if (!postsRepository.existsById(postId)) {
          return; // 🔥 삭제된 글이면 카운트 증가 안함
      }
      postsRepository.increaseCnt(postId);
  }

  /* ===============================
     조회 (DTO / Entity)
  =============================== */

  private Posts findPostsEntityById(long postId) {
    return postsRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));
  }

  /**
   * ✅ 핵심 수정:
   * - postsRepository.findByPostId(postId)가 null이면 .toDTO()에서 NPE 터짐
   * - 컨트롤러에서 null이면 404 처리하므로 여기서는 null 반환
   */
  public PostsDTO findByPostId(long postId) {
    Posts post = postsRepository.findByPostId(postId);

    // 🔥 삭제/없는 글이면 null 반환 (컨트롤러가 404 처리)
    if (post == null) return null;

    PostsDTO dto = post.toDTO();

    // dto.getSize1()가 null일 수도 있으면 방어
    try {
      dto.setSize1_label(Tool.unit(dto.getSize1()));
    } catch (Exception e) {
      dto.setSize1_label("");
    }

    return dto;
  }

  /* ===============================
     파일 삭제 공통
  =============================== */

  private void deletePostFiles(Posts posts) {
    String dir = Tool.getServerDir("posts");

    if (posts.getFile1saved() != null && !posts.getFile1saved().equals("none1.png")) {
      Tool.deleteFile(dir, posts.getFile1saved());
    }

    if (posts.getThumb1() != null && !posts.getThumb1().equals("none1.png")) {
      Tool.deleteFile(dir, posts.getThumb1());
    }
  }

  /* ===============================
     텍스트 수정 → 🔥 임베딩 재생성
  =============================== */

  public int update_text(String title, String content, String word, long postId) {
    int result = postsRepository.update_text(title, content, word, postId);

    if (result > 0) {
      try {
        postsEmbeddingService.updateEmbedding(
            postId,
            (title == null ? "" : title) + " " + (content == null ? "" : content)
        );
      } catch (Exception e) {
        System.out.println("⚠ 임베딩 업데이트 실패 (글 수정은 유지) postId=" + postId);
        e.printStackTrace();
      }
    }

    return result;
  }

  public int password_check(long postId, String password) {
    return postsRepository.password_check(postId, password);
  }

  public int update_file1(String file1, String file1saved, String thumb1, long size1, long postId) {
    return postsRepository.update_file1(file1, file1saved, thumb1, size1, postId);
  }

  /* ===============================
     게시글 삭제
  =============================== */

  public int delete(long postId, String password) {

    List<Long> commentIds = commentsRepository.findCommentIdsByPostId(postId);
    if (!commentIds.isEmpty()) {
        commentsReportRepository.deleteByCommentIds(commentIds);
    }

    commentsRepository.softDeleteAllByPostId(postId);

    Posts posts = findPostsEntityById(postId);
    deletePostFiles(posts);

    postTagService.replaceTags(postId, List.of());

    return postsRepository.delete(postId, password);
}



  public int adminDelete(long postId, String requestUserId) {

    int grade = userService.getUserGrade(requestUserId);
    if (grade != 2) {
        throw new SecurityException("관리자 권한이 없습니다.");
    }

    List<Long> commentIds = commentsRepository.findCommentIdsByPostId(postId);
    if (!commentIds.isEmpty()) {
        commentsReportRepository.deleteByCommentIds(commentIds);
    }

    commentsRepository.softDeleteAllByPostId(postId);

    Posts posts = findPostsEntityById(postId);
    deletePostFiles(posts);

    postTagService.replaceTags(postId, List.of());

    postsRepository.deleteById(postId);
    return 1;
}


  /* ===============================
     관리자 수정 → 🔥 임베딩 재생성
  =============================== */

  public int update_text_admin(String title, String content, String word, long postId) {
    int result = postsRepository.update_text_admin(title, content, word, postId);

    if (result > 0) {
      try {
        postsEmbeddingService.updateEmbedding(
            postId,
            (title == null ? "" : title) + " " + (content == null ? "" : content)
        );
      } catch (Exception e) {
        System.out.println("⚠ 임베딩 업데이트 실패 (관리자 글 수정은 유지) postId=" + postId);
        e.printStackTrace();
      }
    }

    return result;
  }

  public int update_file1_admin(String file1, String file1saved, String thumb1, long size1, long postId) {
    return postsRepository.update_file1_admin(file1, file1saved, thumb1, size1, postId);
  }

  /* ===============================
     Youtube
  =============================== */

  public int youtube(String youtube, long postId, String password) {
    return postsRepository.youtube(youtube, postId, password);
  }

  public int youtubeAdmin(String youtube, long postId) {
    return postsRepository.youtubeAdmin(youtube, postId);
  }

  /* ===============================
     Map
  =============================== */

  public int map(String map, long postId, String password) {
    return postsRepository.map(map, postId, password);
  }

  public int mapAdmin(String map, long postId) {
    return postsRepository.mapAdmin(map, postId);
  }

  /* ===============================
     태그
  =============================== */

  public void replaceTags(long postId, List<String> tags) {
    postTagService.replaceTags(postId, tags);
  }

  /* ===============================
     태그 검색
  =============================== */

  public Page<Posts> searchByTags(
          List<String> tags,
          String mode,
          Pageable pageable
  ) {
      if (tags == null || tags.isEmpty()) {
          return Page.empty(pageable);
      }

      if ("OR".equalsIgnoreCase(mode)) {
          return postsRepository.findByTagsOr(tags, pageable);
      }

      return postsRepository.findByTagsAnd(tags, tags.size(), pageable);
  }

}
