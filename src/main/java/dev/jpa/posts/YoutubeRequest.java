// dev.jpa.posts.YoutubeRequest
package dev.jpa.posts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YoutubeRequest {
    private Long postId;
    private String youtube;
    private String password;
    private String requestUserId;
}
