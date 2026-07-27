package dev.jpa.ai.user_profile_embedding;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "USER_PROFILE_EMBEDDINGS")
@Data
public class UserProfileEmbedding {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Lob
    @Column(name = "EMBEDDING")
    private String embedding;
}
