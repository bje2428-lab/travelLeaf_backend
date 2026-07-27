package dev.jpa.posts_reaction;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsReactionDTO {

    private String reactionId;
    private String userId;
    private Long postId;
    private String type;   // like / favorite
}
