package dev.jpa.posts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDeleteRequest {
    private Long postId;
    private String password;
    private String requestUserId;
}
