package dev.jpa.comments;

import lombok.Data;

@Data
public class CommentsDTO {

    private Long commentId;
    private Long postId;
    private Long parentCommentId;
    private String userId;

    private String content;
    private String imageUrl;
}
