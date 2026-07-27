package dev.jpa.posts_tags;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostTagId implements Serializable {

    private long postId;   // 🔥 Posts.postId 와 타입 동일
    private long tagId;    // 🔥 Tag.tagId 와 타입 동일
}
