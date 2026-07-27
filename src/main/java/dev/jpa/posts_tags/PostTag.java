package dev.jpa.posts_tags;

import dev.jpa.tags.Tag;
import dev.jpa.posts.Posts;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POSTS_TAGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

    @EmbeddedId
    private PostTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
