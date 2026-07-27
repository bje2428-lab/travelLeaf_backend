package dev.jpa.comments_reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentReactionService {

    private final CommentReactionRepository repo;

    /** 댓글 좋아요 토글 */
    public String toggle(String userId, Long commentId){

        var exist = repo.findByUserIdAndCommentId(userId, commentId);

        if(exist.isPresent()){
            repo.delete(exist.get());
            return "UNLIKED";
        }

        CommentReaction cr = CommentReaction.builder()
                .reactionId("CR-" + userId + "-" + commentId)
                .userId(userId)
                .commentId(commentId)
                .build();

        repo.save(cr);
        return "LIKED";
    }

    /** 좋아요 수 */
    public int count(Long commentId){
        return repo.countByCommentId(commentId);
    }

    /** 내가 눌렀는지 확인 */
    public boolean isLiked(String userId, Long commentId){
        return repo.existsByUserIdAndCommentId(userId, commentId);
    }
}
