package dev.jpa.comments_reaction;

import lombok.Data;

@Data
public class CommentReactionDTO {
    private String userId;
    private Long commentId;
}
