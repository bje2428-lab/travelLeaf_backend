package dev.jpa.posts_summary;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "POSTS_SUMMARY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostsSummary {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Lob
    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "keywords")
    private String keywords;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
