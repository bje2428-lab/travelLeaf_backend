package dev.jpa.review;

import lombok.Data;

@Data
public class TranslateReviewResponse {
    private boolean ok;
    private String targetLang;
    private String placeName;
    private String content;
    private String reason; // 실패 이유(키 없음, 파싱 실패 등)
}
