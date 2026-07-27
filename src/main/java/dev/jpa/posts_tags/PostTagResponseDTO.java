package dev.jpa.posts_tags;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostTagResponseDTO {
    private Long tagId;
    private String name;
}
