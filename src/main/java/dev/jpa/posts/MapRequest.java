package dev.jpa.posts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapRequest {
    private Long postId;
    private String map;          // 저장할 지도 값(iframe src 또는 url)
    private String password;     // 일반회원용
    private String requestUserId; // 관리자용
}
