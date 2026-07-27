package dev.jpa.review;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "REVIEW_COMMENT_LIKE",
        uniqueConstraints = @UniqueConstraint(columnNames = {"COMMENT_ID", "USER_ID"})
)
@SequenceGenerator(
        name = "review_comment_like_seq_generator",
        sequenceName = "REVIEW_COMMENT_LIKE_SEQ",
        allocationSize = 1
)

public class ReviewCommentLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_comment_like_seq_generator")
    @Column(name = "LIKE_ID")
    private Long likeId;

    @Column(name = "COMMENT_ID", nullable = false)
    private Long commentId;

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
