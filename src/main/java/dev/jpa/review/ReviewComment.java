package dev.jpa.review;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "REVIEW_COMMENT")
@SequenceGenerator(
        name = "review_comment_seq_generator",
        sequenceName = "REVIEW_COMMENT_SEQ",
        allocationSize = 1
)
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_comment_seq_generator")
    @Column(name = "COMMENT_ID")
    private Long commentId;

    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;

    @Column(name = "IS_DELETED", nullable = false)
    private Integer isDeleted = 0; // 0/1

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    


}
