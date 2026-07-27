package dev.jpa.posts_embeddings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimilarPostDTO {
    private long postId;
    private double score;
}
