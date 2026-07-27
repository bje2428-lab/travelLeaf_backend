package dev.jpa.posts_summary;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostsSummaryDTO {

    private Long postId;
    private String summary;
    private String keywords;
}
