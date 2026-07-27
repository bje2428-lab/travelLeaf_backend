package dev.jpa.comments_reaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    boolean existsByUserIdAndCommentId(String userId, Long commentId);

    Optional<CommentReaction> findByUserIdAndCommentId(String userId, Long commentId);

    int countByCommentId(Long commentId);
}
