package dev.jpa.comments;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Entity
@Table(name = "COMMENTS")
@SequenceGenerator(
        name = "comments_seq_generator",
        sequenceName = "COMMENTS_SEQ",
        allocationSize = 1
)
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_seq_generator")
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_deleted", length = 1)
    private String isDeleted = "N";

    // ✅ 자동 생성
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    // ✅ 자동 수정
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;


    /* =========================================================
       ⭐ 좋아요 정렬 / 표시용 필드
       (DB 컬럼 아님, 조회 시 동적으로 채움)
    ========================================================= */
    @Transient
    private long likeCount;
}
