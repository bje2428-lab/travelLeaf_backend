package dev.jpa.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewCommentResponseDTO {
    private Long commentId;
    private Long reviewId;
    private String userId;
    private String content;

    private Integer isDeleted;
    private LocalDateTime createdAt;

    private long likeCount;
    private boolean likedByMe; // 조회하는 userId 기준
}
