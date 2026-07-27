package dev.jpa.posts;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUpdateTextRequest {
    private Long postId;
    private String title;
    private String content;
    private String word;
    private String password;
    private String requestUserId;
    private List<String> tags;
}
