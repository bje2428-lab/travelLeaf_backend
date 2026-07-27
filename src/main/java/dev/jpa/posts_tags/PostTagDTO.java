package dev.jpa.posts_tags;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTagDTO {
    private Long postId;
    private Long tagId;
}
