package dev.jpa.posts;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import dev.jpa.posts_quality.PostsQualityService;


import dev.jpa.tool.Tool;
import dev.jpa.tool.Upload;
import dev.jpa.user.UserService;

@RestController
@RequestMapping("/posts")
public class PostsCont {

  @Autowired
  PostsService postsService;

  @Autowired
  private UserService userService;

  @Autowired
  private PostsQualityService postsQualityService;

  public PostsCont() {
    System.out.println("-> PostsCont created.");
    
  }

  /** 전체 목록 조회 */
  @GetMapping(path = "/list_all/{cateno}")
  public ResponseEntity<List<Posts>> list_all(@PathVariable("cateno") Long cateno) {
    return ResponseEntity.ok(postsService.findByCatenoOrderByPostIdDesc(cateno));
  }

  /**
   * 페이징 + 검색 + 정렬
   * sort = latest | likes | views
   */
  @GetMapping(path = "/list_all_paging_search")
  public ResponseEntity<PageResponse<Posts>> list_all_paging(
      @RequestParam(name = "cateno", defaultValue = "0") long cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {

      Pageable pageable = PageRequest.of(page, size);   // 👉 정렬 제거!!

      Page<Posts> p = this.postsService.list_all_paging_search(cateno, word, pageable);

      return ResponseEntity.ok(
          new PageResponse<>(
              p.getContent(),
              p.getNumber(),
              p.getSize(),
              p.getTotalElements(),
              p.getTotalPages()
          )
      );
  }


  /**
   * 페이징 + 정렬
   * sort = latest | likes | views
   */
  @GetMapping(path = "/list_all_paging")
  public ResponseEntity<PageResponse<Posts>> list_all_paging(
      @RequestParam(name = "cateno", defaultValue = "0") long cateno,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      @RequestParam(name = "sort", defaultValue = "latest") String sort) {

    Sort sorting;
    switch (sort) {
      case "likes":
        sorting = Sort.by("recom").descending();
        break;
      case "views":
        sorting = Sort.by("cnt").descending();
        break;
      default:
        sorting = Sort.by("postId").descending();
    }

    Pageable pageable = PageRequest.of(page, size, sorting);
    Page<Posts> p = postsService.findByCateno(cateno, pageable);


    return ResponseEntity
        .ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
  }

  /** 게시글 등록 */
  @PostMapping(value = "/create")
  public ResponseEntity<Posts> create(@ModelAttribute("postsDTO") PostsDTO postsDTO) {
    String file1 = "";
    String file1saved = "";
    String thumb1 = "";

    String upDir = Tool.getServerDir("posts");

    MultipartFile mf = postsDTO.getFile1MF();

    if (mf != null) {
      file1 = mf.getOriginalFilename();
      long size1 = mf.getSize();
      if (size1 > 0 && Tool.checkUploadFile(file1)) {
        file1saved = Upload.saveFileSpring(mf, upDir);
        if (Tool.isImage(file1saved)) {
          thumb1 = Tool.preview(upDir, file1saved, 200, 150);
        }
        postsDTO.setFile1(file1);
        postsDTO.setFile1saved(file1saved);
        postsDTO.setThumb1(thumb1);
        postsDTO.setSize1(size1);
      }
    } else {
      postsDTO.setFile1("none1.png");
      postsDTO.setFile1saved("none1.png");
      postsDTO.setThumb1("none1.png");
      postsDTO.setSize1((int) (23.1 * 1024));
    }

    Posts post = this.postsService.save(postsDTO);
    postsQualityService.analyzePost(post.getPostId());
    return ResponseEntity.ok(post);
  }

  /** 조회 */
  @GetMapping("/read/{postId}")
  public ResponseEntity<?> findByPostId(
          @PathVariable("postId") Long postId
  ) {

      PostsDTO postsDTO = postsService.findByPostId(postId);

      // 🔥 먼저 존재 확인
      if (postsDTO == null) {
          return ResponseEntity.status(404).body("POST_NOT_FOUND");
      }

      // 🔥 존재할 때만 조회수 증가
      postsService.increaseCnt(postId);

      return ResponseEntity.ok(postsDTO);
  }



 /**
  * http://localhost:9100/posts/update_text

  * @param title
  * @param content
  * @param word
  * @param postId
  * @param password
  * @param requestUserId
  * @param tags
  * @return
  */
  
  @PostMapping("/update_text")
  public ResponseEntity<Integer> update_text(
      @RequestBody PostUpdateTextRequest req
  ) {

      Long postId = req.getPostId();
      String title = req.getTitle();
      String content = req.getContent();
      String word = req.getWord();
      String password = req.getPassword();
      String requestUserId = req.getRequestUserId();
      List<String> tags = req.getTags();

      System.out.println("==== 글 수정 진입 ====");
      System.out.println("postId = " + postId);
      System.out.println("password = " + password);
      System.out.println("requestUserId = " + requestUserId);

      if (postId == null || postId <= 0) {
          return ResponseEntity.badRequest().body(0);
      }

      // ===============================
      // 1️⃣ 관리자 수정
      // ===============================
      if (requestUserId != null && !requestUserId.isEmpty()) {
          int grade = userService.getUserGrade(requestUserId);
          if (grade != 2) {
              return ResponseEntity.status(403).body(0);
          }

          int sw = postsService.update_text_admin(title, content, word, postId);
          postsService.replaceTags(postId, tags);
          postsQualityService.analyzePost(postId);
          return ResponseEntity.ok(sw);
      }

      // ===============================
      // 2️⃣ 일반회원 수정
      // ===============================
      int cnt = postsService.password_check(postId, password);
      if (cnt != 1) {
          return ResponseEntity.ok(2); // 비밀번호 불일치
      }

      int sw = postsService.update_text(title, content, word, postId);
      postsService.replaceTags(postId, tags);
      postsQualityService.analyzePost(postId);
      return ResponseEntity.ok(sw);
  }


  /**
   * 파일 수정 POST http://localhost:9100/posts/update_file1 Query Params: postId =
   * 수정할 게시글 번호 (예: 3) password = 게시글 비밀번호 (예: "123") Body: form-data (Multipart)
   * file1MF = 수정할 파일 (MultipartFile) Response: 0 = 수정 실패 1 = 수정 성공 2 = 패스워드 불일치 3
   * = 전송할 파일 없음
   */
  @PostMapping("/update_file1")
  public ResponseEntity<Integer> update_file1(
      @RequestParam(name = "file1MF", required = false) MultipartFile file1MF,
      @RequestParam(name = "postId", defaultValue = "0") long postId,
      @RequestParam(name = "password", defaultValue = "") String password,
      @RequestParam(name = "requestUserId", required = false) String requestUserId
  ) {

      System.out.println("==== update_file1 진입 ====");
      System.out.println("postId = " + postId);
      System.out.println("password = " + password);
      System.out.println("requestUserId = " + requestUserId);
      System.out.println("file1MF = " + file1MF);

      // ===============================
      // 1️⃣ 관리자
      // ===============================
      if (requestUserId != null && !requestUserId.isEmpty()) {
          try {
              int grade = userService.getUserGrade(requestUserId);
              if (grade != 2) return ResponseEntity.status(403).body(0);

              if (file1MF == null || file1MF.isEmpty()) {
                  return ResponseEntity.ok(3); // 파일 없음
              }

              String upDir = Tool.getServerDir("posts");

              PostsDTO postsDTO = postsService.findByPostId(postId);
              Tool.deleteFile(upDir, postsDTO.getFile1saved());
              Tool.deleteFile(upDir, postsDTO.getThumb1());

              String file1 = file1MF.getOriginalFilename();
              long size1 = file1MF.getSize();

              String file1saved = "";
              String thumb1 = "";

              if (size1 > 0 && Tool.checkUploadFile(file1)) {
                  file1saved = Upload.saveFileSpring(file1MF, upDir);
                  if (Tool.isImage(file1saved)) {
                      thumb1 = Tool.preview(upDir, file1saved, 200, 150);
                  }
              }

              int cnt = postsService.update_file1_admin(
                  file1, file1saved, thumb1, size1, postId
              );
              return ResponseEntity.ok(cnt);

          } catch (Exception e) {
              e.printStackTrace();
              return ResponseEntity.status(500).body(0);
          }
      }

      // ===============================
      // 2️⃣ 일반회원
      // ===============================
      int cnt = postsService.password_check(postId, password);
      if (cnt != 1) {
          return ResponseEntity.ok(2); // 비밀번호 불일치
      }

      if (file1MF == null || file1MF.isEmpty()) {
          return ResponseEntity.ok(3); // 파일 없음
      }

      String upDir = Tool.getServerDir("posts");

      PostsDTO postsDTO = postsService.findByPostId(postId);
      Tool.deleteFile(upDir, postsDTO.getFile1saved());
      Tool.deleteFile(upDir, postsDTO.getThumb1());

      String file1 = file1MF.getOriginalFilename();
      long size1 = file1MF.getSize();

      String file1saved = "";
      String thumb1 = "";

      if (size1 > 0 && Tool.checkUploadFile(file1)) {
          file1saved = Upload.saveFileSpring(file1MF, upDir);
          if (Tool.isImage(file1saved)) {
              thumb1 = Tool.preview(upDir, file1saved, 200, 150);
          }
      }

      int sw = postsService.update_file1(
          file1, file1saved, thumb1, size1, postId
      );

      return ResponseEntity.ok(sw);
  }



  /**
   * 파일 삭제 - 관리자: 비밀번호 없이 삭제 가능 - 일반회원: 비밀번호 필요
   */
  @PostMapping("/delete_file1")
  public ResponseEntity<Integer> delete_file1(
      @RequestBody FileDeleteRequest req
  ) {

      Long postId = req.getPostId();
      String password = req.getPassword();
      String requestUserId = req.getRequestUserId();

      System.out.println("==== delete_file1 진입 ====");
      System.out.println("postId = " + postId);
      System.out.println("password = " + password);
      System.out.println("requestUserId = " + requestUserId);

      try {
          // ===============================
          // 1️⃣ 관리자 삭제
          // ===============================
          if (requestUserId != null && !requestUserId.isEmpty()) {
              int grade = userService.getUserGrade(requestUserId);
              if (grade != 2) {
                  return ResponseEntity.status(403).body(0);
              }

              PostsDTO postsDTO = postsService.findByPostId(postId);
              String dir = Tool.getServerDir("posts");

              if (!"none1.png".equals(postsDTO.getFile1saved())) {
                  Tool.deleteFile(dir, postsDTO.getFile1saved());
                  Tool.deleteFile(dir, postsDTO.getThumb1());

                  int cnt = postsService.update_file1_admin(
                      "none1.png", "none1.png", "none1.png", 0, postId
                  );
                  return ResponseEntity.ok(cnt);
              }
              return ResponseEntity.ok(3);
          }

          // ===============================
          // 2️⃣ 일반회원 삭제
          // ===============================
          int cnt = postsService.password_check(postId, password);
          if (cnt != 1) {
              return ResponseEntity.ok(2); // ❗ 여기서 이제 진짜 비번 체크
          }

          PostsDTO postsDTO = postsService.findByPostId(postId);
          String dir = Tool.getServerDir("posts");

          if (!"none1.png".equals(postsDTO.getFile1saved())) {
              Tool.deleteFile(dir, postsDTO.getFile1saved());
              Tool.deleteFile(dir, postsDTO.getThumb1());

              int r = postsService.update_file1(
                  "none1.png", "none1.png", "none1.png", 0, postId
              );
              return ResponseEntity.ok(r);
          }

          return ResponseEntity.ok(3);

      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(500).body(0);
      }
  }



  /**
   * 게시글 삭제 API (작성자 또는 관리자) - 작성자는 비밀번호 일치 시 삭제 가능 (파일 삭제 포함) - 관리자(grade=2)는
   * 비밀번호와 상관없이 삭제 가능 (파일 삭제 포함)
   *
   * @param postId        삭제할 게시글의 ID
   * @param password      작성자가 제공한 게시글 비밀번호 (관리자 삭제 시에는 무시됨)
   * @param requestUserId (옵션) 삭제를 요청하는 사용자의 ID (이 유저의 grade가 2인지 서비스에서 확인)
   * @return 0 = 삭제 실패, 1 = 삭제 성공, 2 = 패스워드 불일치
   *
   *         ### Postman 요청 예시 ###
   *
   *         **1. 작성자 삭제 (비밀번호 사용):** - **HTTP Method:** `POST` - **URL:**
   *         `http://localhost:9100/posts/delete?postId=12&password=1234` -
   *         **Body:** `none` - **Response:** `1` (성공), `2` (패스워드 불일치), `HTTP 404`
   *         (게시글 없음)
   *
   *         **2. 관리자 삭제 (requestUserId 사용):** - **HTTP Method:** `POST` -
   *         **URL:**
   *         `http://localhost:9100/posts/delete?postId=12&requestUserId=admin_user_id`
   *         (여기서 `12`는 삭제할 `postId`) (`admin_user_id`는 실제 `grade=2`인 유저의 ID) -
   *         **Body:** `none` - **Response:** `1` (성공), `HTTP 403` (관리자 권한 부족),
   *         `HTTP 404` (게시글 없음) 등
   */
  @PostMapping(path = "/delete")
  public ResponseEntity<Integer> delete(@RequestParam(name = "postId", defaultValue = "0") long postId,
      @RequestParam(name = "password", defaultValue = "") String password,
      @RequestParam(name = "requestUserId", required = false) String requestUserId) { // ⭐️ requestUserId만 받도록 변경

    int sw = 0;

    // ⭐️ 1. requestUserId가 제공되었다면, 관리자 삭제 요청으로 간주하고 서비스에 위임
    if (requestUserId != null && !requestUserId.isEmpty()) {
      System.out.println("-> 관리자/인증 사용자 게시글 삭제 요청: postId=" + postId + ", by user=" + requestUserId);
      try {
        // 서비스에서 requestUserId의 grade를 확인하고, 관리자면 삭제 실행
        sw = postsService.adminDelete(postId, requestUserId); // ⭐️ postsService의 adminDelete 호출 (requestUserId만 전달)
      } catch (SecurityException e) {
        System.err.println("권한 오류: " + e.getMessage());
        return ResponseEntity.status(403).body(0); // 403 Forbidden (권한 없음)
      } catch (IllegalArgumentException e) {
        System.err.println("게시글을 찾을 수 없음: " + e.getMessage());
        return ResponseEntity.status(404).body(0); // 404 Not Found
      } catch (Exception e) {
        System.err.println("게시글 관리자 삭제 중 오류 발생: " + e.getMessage());
        return ResponseEntity.status(500).body(0); // 500 Internal Server Error
      }
    } else {
      // ⭐️ 2. requestUserId가 없거나 비어있으면, 기존 작성자 삭제 로직 수행 (비밀번호 확인)
      System.out.println("-> 작성자 게시글 삭제 요청: postId=" + postId + ", password=" + password);
      int cnt = postsService.password_check(postId, password); // 비밀번호 확인

      if (cnt == 1) { // 비밀번호 일치
        try {
          sw = postsService.delete(postId, password); // ⭐️ postsService의 delete 호출 (password 포함)
        } catch (IllegalArgumentException e) {
          System.err.println("게시글을 찾을 수 없음: " + e.getMessage());
          return ResponseEntity.status(404).body(0); // 404 Not Found
        } catch (Exception e) {
          System.err.println("게시글 작성자 삭제 중 오류 발생: " + e.getMessage());
          return ResponseEntity.status(500).body(0); // 500 Internal Server Error
        }
      } else {
        sw = 2; // 패스워드 불일치
      }
    }

    return ResponseEntity.ok(sw);
  }

  @PostMapping("/youtube")
  public ResponseEntity<Integer> youtube(
      @RequestBody YoutubeRequest req
  ) {

      Long postId = req.getPostId();
      String youtube = req.getYoutube();
      String password = req.getPassword();
      String requestUserId = req.getRequestUserId();

      System.out.println("==== youtube 수정 진입 ====");
      System.out.println("postId = " + postId);
      System.out.println("youtube = " + youtube);
      System.out.println("password = " + password);
      System.out.println("requestUserId = " + requestUserId);

      // ===============================
      // 1️⃣ 관리자 수정
      // ===============================
      if (requestUserId != null && !requestUserId.isEmpty()) {
          int grade = userService.getUserGrade(requestUserId);
          if (grade != 2) {
              return ResponseEntity.status(403).body(0);
          }

          int sw = postsService.youtubeAdmin(youtube, postId);
          return ResponseEntity.ok(sw);
      }

      // ===============================
      // 2️⃣ 일반회원 수정
      // ===============================
      int cnt = postsService.password_check(postId, password);
      if (cnt != 1) {
          return ResponseEntity.ok(2); // 비밀번호 불일치
      }

      int sw = postsService.youtube(youtube, postId, password);
      return ResponseEntity.ok(sw);
  }

  /**
   * Map 변경 POST http://localhost:9100/posts/map Query Params: postId = 수정할 게시글 번호
   * (예: 31) password = 게시글 비밀번호 (예: "1234") map = 변경할 지도 스크립트 (예: "<iframe...>")
   * Body: 없음 Response: 0 = 수정 실패 1 = 수정 성공 2 = 패스워드 불일치
   */
  @PostMapping("/map")
  public ResponseEntity<Integer> map(@RequestBody MapRequest req) {

      Long postId = req.getPostId();
      String map = req.getMap();
      String password = req.getPassword();
      String requestUserId = req.getRequestUserId();

      System.out.println("==== map 수정 진입 ====");
      System.out.println("postId = " + postId);
      System.out.println("map = " + map);
      System.out.println("password = " + password);
      System.out.println("requestUserId = " + requestUserId);

      // postId 방어
      if (postId == null || postId <= 0) {
          return ResponseEntity.badRequest().body(0);
      }

      // map null 방어 (삭제일 때 빈값 들어올 수 있음)
      if (map == null) map = "";

      // ===============================
      // 1) 관리자 수정/삭제
      // ===============================
      if (requestUserId != null && !requestUserId.isEmpty()) {
          int grade = userService.getUserGrade(requestUserId);
          if (grade != 2) {
              return ResponseEntity.status(403).body(0);
          }

          int sw = postsService.mapAdmin(map, postId);
          return ResponseEntity.ok(sw);
      }

      // ===============================
      // 2) 일반회원 수정/삭제 (비밀번호 필수)
      // ===============================
      int cnt = postsService.password_check(postId, password);
      if (cnt != 1) {
          return ResponseEntity.ok(2); // 비밀번호 불일치
      }

      int sw = postsService.map(map, postId, password);
      return ResponseEntity.ok(sw);
  }

  @Autowired
  private PostsRepository postsRepository;

  /**
   * AI 추천용 게시글 조회
   * 예:
   * /posts/list_by_ids?ids=30&ids=1&ids=25
   */
  @GetMapping("/list_by_ids")
  public ResponseEntity<List<Posts>> listByIds(@RequestParam(name = "ids") List<Long> ids) {

      if (ids == null || ids.isEmpty()) {
          return ResponseEntity.badRequest().build();
      }

      List<Posts> list = postsRepository.findByPostIdIn(ids);
      return ResponseEntity.ok(list);
  }
}