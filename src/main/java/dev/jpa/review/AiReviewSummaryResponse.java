package dev.jpa.review;

import java.util.List;
import java.util.Map;

public class AiReviewSummaryResponse {
    public String summary1;             // ✅ 1줄 요약
    public List<String> keywords;       // 키워드
    public Map<String, String> tags;    // 태그
}
