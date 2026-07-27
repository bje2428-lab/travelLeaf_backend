package dev.jpa.review;

import lombok.Data;

@Data
public class ReviewCommentCreateDTO {
    private String userId;
    private String content;
}
