package dev.jpa.comments;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentsCont {

    @Autowired
    private CommentsService commentsService;


    /* ===============================
       댓글 등록 (세션 기반)
    =============================== */
    @PostMapping("/create")
    public ResponseEntity<Comments> create(@RequestBody CommentsDTO dto) {
        return ResponseEntity.ok(
            commentsService.create(dto.getUserId(), dto)
        );
    }


    /* ===============================
       기존 목록
    =============================== */
    @GetMapping("/list/{postId}")
    public ResponseEntity<List<Comments>> list(
            @PathVariable("postId") Long postId) {

        return ResponseEntity.ok(
                commentsService.listByPost(postId)
        );
    }


    /* =========================================================
       ⭐ 트리 구조 + 정렬 옵션 지원
       ---------------------------------------------------------
       🔥 기본 호출
       GET /comments/list/tree/{postId}

       🔥 정렬 추가
       GET /comments/list/tree/{postId}?sort=latest
       GET /comments/list/tree/{postId}?sort=likes

       sort 옵션:
       - oldest  (기본값)
       - latest
       - likes   ⭐ 좋아요 많은 순
    ========================================================= */
    @GetMapping("/list/tree/{postId}")
    public ResponseEntity<List<Comments>> listTree(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "sort", required = false, defaultValue = "oldest")
            String sort
    ) {
        return ResponseEntity.ok(
                commentsService.listTreeByPost(postId, sort)
        );
    }


    /* ===============================
       페이징
    =============================== */
    @GetMapping("/list/{postId}/paging")
    public ResponseEntity<?> listPaging(
            @PathVariable("postId") Long postId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        return ResponseEntity.ok(
                commentsService.listByPostPaging(postId, page, size)
        );
    }


    /* ===============================
       댓글 수정
    =============================== */
    @PostMapping("/update/{commentId}")
    public ResponseEntity<Comments> update(
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentsDTO dto) {

        return ResponseEntity.ok(
                commentsService.update(commentId, dto.getUserId(), dto)
        );
    }



    /* ===============================
       댓글 삭제 (작성자)
    =============================== */
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> delete(
            @PathVariable("commentId") Long commentId,
            HttpSession session) {

        String userId = (String) session.getAttribute("userId");
        commentsService.delete(commentId, userId);
        return ResponseEntity.ok("deleted");
    }


    /* ===============================
       관리자 삭제
    =============================== */
    @DeleteMapping("/admin/delete/{commentId}")
    public ResponseEntity<String> adminDelete(
            @PathVariable("commentId") Long commentId,
            @RequestParam("requestUserId") String requestUserId,
            @RequestParam("requestUserGrade") int requestUserGrade
    ) {
        commentsService.adminDelete(
                commentId,
                requestUserId,
                requestUserGrade
        );
        return ResponseEntity.ok("admin deleted");
    }


    /* ===============================
       관리자: 게시글 전체 삭제
    =============================== */
    @DeleteMapping("/admin/delete-all-by-post/{postId}")
    public ResponseEntity<String> adminDeleteAll(
            @PathVariable("postId") Long postId,
            @RequestParam("requestUserId") String requestUserId,
            @RequestParam("requestUserGrade") int requestUserGrade
    ) {
        commentsService.adminDeleteAllCommentsByPost(
                postId,
                requestUserId,
                requestUserGrade
        );
        return ResponseEntity.ok("all deleted");
    }
}
