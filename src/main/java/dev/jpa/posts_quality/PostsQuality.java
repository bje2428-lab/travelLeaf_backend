package dev.jpa.posts_quality;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "POSTS_QUALITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostsQuality {

    @Id
    @Column(name = "POST_ID")
    private Long postId;

    @Column(name = "READABILITY")
    private Double readability;

    @Column(name = "ORIGINALITY")
    private Double originality;

    @Column(name = "USEFULNESS")
    private Double usefulness;

    @Column(name = "AI_SCORE")
    private Double aiScore;

    // 🔥 스팸 확률 (0~100)
    @Column(name = "SPAM_SCORE")
    private Double spamScore;

    // 🔥 게시물 등급 (HIGH, MID, LOW, SPAM)
    @Column(name = "QUALITY_GRADE")
    private String qualityGrade;

    @Column(name = "ANALYZED_AT")
    private LocalDateTime analyzedAt;
}
