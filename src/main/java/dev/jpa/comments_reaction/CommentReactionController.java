package dev.jpa.comments_reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment-reactions")   // ⭐ 기본 URL : http://localhost:9100/comment-reactions
@RequiredArgsConstructor
public class CommentReactionController {

    private final CommentReactionService service;


    /* =========================================================
       댓글 좋아요 토글
       ---------------------------------------------------------
       🔥 POST 요청
       http://localhost:9100/comment-reactions/toggle

       🔥 Body(JSON)
       {
         "userId": "user1",
         "commentId": 10
       }

       ✔️ 동작
       - 아직 좋아요 안 누름 → 좋아요 등록  → "LIKED"
       - 이미 좋아요 눌렀음 → 좋아요 취소  → "UNLIKED"
    ========================================================= */
    @PostMapping("/toggle")
    public String toggle(@RequestBody CommentReactionDTO dto){
        return service.toggle(dto.getUserId(), dto.getCommentId());
    }



    /* =========================================================
       특정 댓글 좋아요 개수 조회
       ---------------------------------------------------------
       🔥 GET 요청
       http://localhost:9100/comment-reactions/count/{commentId}

       예)
       http://localhost:9100/comment-reactions/count/10

       ✔️ 결과
       int 숫자 반환
    ========================================================= */
    @GetMapping("/count/{commentId}")
    public int count(@PathVariable("commentId") Long commentId){
        return service.count(commentId);
    }



    /* =========================================================
       특정 사용자가 해당 댓글 좋아요 눌렀는지 확인
       ---------------------------------------------------------
       🔥 GET 요청
       http://localhost:9100/comment-reactions/check?userId=user1&commentId=10

       ✔️ 결과
       true  → 좋아요 누름
       false → 좋아요 안 누름
    ========================================================= */
    @GetMapping("/check")
    public boolean check(
            @RequestParam("userId") String userId,
            @RequestParam("commentId") Long commentId
    ){
        return service.isLiked(userId, commentId);
    }
}
