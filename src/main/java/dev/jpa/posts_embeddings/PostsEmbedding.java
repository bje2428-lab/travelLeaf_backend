package dev.jpa.posts_embeddings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "POSTS_EMBEDDINGS")
public class PostsEmbedding {

    @Id
    @Column(name = "post_id")
    private Long postsId;

    @Lob
    @Basic(fetch = FetchType.EAGER)   // 🔥 이 한 줄이 핵심
    @Column(name = "embedding", nullable = false)
    private String embedding;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
}
