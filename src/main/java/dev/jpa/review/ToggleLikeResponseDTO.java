package dev.jpa.review;

import lombok.Data;

@Data
public class ToggleLikeResponseDTO {
    private boolean liked;   // 토글 후 상태
    private long likeCount;  // 최신 좋아요 수
}
