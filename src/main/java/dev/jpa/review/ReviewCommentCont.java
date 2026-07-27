package dev.jpa.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
public class ReviewCommentCont {

    @Autowired private ReviewCommentService commentService;

    // 댓글 작성
    // POST /review/{reviewId}/comment
    @PostMapping("/{reviewId}/comment")
    public ResponseEntity<ReviewCommentResponseDTO> createComment(
            @PathVariable("reviewId") Long reviewId,
            @RequestBody ReviewCommentCreateDTO dto
    ) {
        return ResponseEntity.ok(commentService.create(reviewId, dto));
    }

    // 댓글 목록 (+좋아요수 + 내가 눌렀는지)
    // GET /review/{reviewId}/comments?page=0&size=20&userId=aaa (userId는 선택)
    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<Page<ReviewCommentResponseDTO>> listComments(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(commentService.list(reviewId, userId, page, size));
    }

    // 댓글 삭제 (작성자만)
    // DELETE /review/comment/{commentId}?userId=aaa
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("userId") String userId
    ) {
        commentService.delete(commentId, userId);
        return ResponseEntity.ok("deleted");
    }

    // 댓글 좋아요 토글
    // POST /review/comment/{commentId}/like?userId=aaa
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<ToggleLikeResponseDTO> toggleLike(
            @PathVariable("commentId") Long commentId,
            @RequestParam("userId") String userId
    ) {
        return ResponseEntity.ok(commentService.toggleLike(commentId, userId));
    }
}
