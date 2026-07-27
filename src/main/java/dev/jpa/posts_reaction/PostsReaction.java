package dev.jpa.posts_reaction;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(
    name = "POST_REACTIONS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_post_reactions",
            columnNames = {"user_id", "post_id", "type"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_reaction_seq")
    @SequenceGenerator(
        name = "post_reaction_seq",
        sequenceName = "POST_REACTIONS_SEQ",
        allocationSize = 1
    )
    @Column(name="reaction_no")
    private Long reactionNo;

    @Column(name="reaction_id", unique = true, nullable = false)
    private String reactionId;

    @Column(name="user_id", nullable = false)
    private String userId;

    @Column(name="post_id", nullable = false)
    private Long postId;

    @Column(name="type", nullable = false)
    private String type; // like / favorite

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_at")
    private Date createdAt;
}
