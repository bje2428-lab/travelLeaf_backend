package dev.jpa.comments_reaction;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(
        name = "COMMENT_REACTIONS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_comment_like_only",
                        columnNames = {"user_id", "comment_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_reaction_seq")
    @SequenceGenerator(
            name = "comment_reaction_seq",
            sequenceName = "COMMENT_REACTIONS_SEQ",
            allocationSize = 1
    )
    @Column(name = "reaction_no")
    private Long reactionNo;

    @Column(name = "reaction_id", unique = true, nullable = false)
    private String reactionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();
}
