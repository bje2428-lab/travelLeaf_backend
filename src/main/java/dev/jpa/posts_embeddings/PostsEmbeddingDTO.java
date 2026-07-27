package dev.jpa.posts_embeddings;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostsEmbeddingDTO {
    private Long postsId;
    private String embedding;
    private String updatedAt;
}
