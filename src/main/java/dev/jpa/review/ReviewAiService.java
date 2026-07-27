package dev.jpa.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ReviewAiService {

    @Value("${openai.api.key:}")
    private String openaiKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    /* =========================
       1) 요약 + 키워드 + 태깅
    ========================= */
    public AiReviewSummaryResponse summarizeAndTag(String placeName, String content) {
        AiReviewSummaryResponse out = new AiReviewSummaryResponse();
        out.summary1 = "";
        out.keywords = List.of();
        out.tags = Map.of();

        boolean hasKey = hasOpenAiKey();
        System.out.println("[AI] OPENAI KEY EXISTS? " + hasKey);
        if (!hasKey) return out;

        String prompt = """
너는 한국어 리뷰 분석기다.
아래 리뷰에서 ①1줄 요약 ②핵심 키워드 ③자동 태깅을 생성해라.

[태그 규칙]
- 분위기: 조용/활기/감성
- 가격: 가성비/보통/비쌈
- 혼잡도: 한산/보통/붐빔
- 난이도(자전거 코스라면): 초급/중급/상급 (해당 없으면 생략)
- 목적: 휴식/뷰/맛집/라이딩/사진 (가장 대표 1개)
- 추천대상: 혼자/커플/가족/아이와/친구 (가장 대표 1개)

[출력 형식]
반드시 JSON만 출력한다. (설명 금지)
{
  "summary1": "...",
  "keywords": ["...", "...", "..."],
  "tags": {
    "분위기": "...",
    "가격": "...",
    "혼잡도": "...",
    "난이도": "...",
    "목적": "...",
    "추천대상": "..."
  }
}

[입력]
장소명: %s
리뷰내용: %s
""".formatted(safe(placeName), safe(content));

        try {
            String text = callOpenAi(prompt, true).trim();
            System.out.println("[AI] parsed output text=" + text);
            if (text.isBlank()) return out;

            JsonNode parsed = om.readTree(text);

            out.summary1 = parsed.path("summary1").asText("");

            List<String> kws = new ArrayList<>();
            for (JsonNode n : parsed.path("keywords")) kws.add(n.asText());
            out.keywords = kws;

            Map<String, String> tags = new LinkedHashMap<>();
            JsonNode tnode = parsed.path("tags");
            if (tnode.isObject()) {
                tnode.fieldNames().forEachRemaining(k -> {
                    String v = tnode.path(k).asText("");
                    if (v != null && !v.isBlank()) tags.put(k, v);
                });
            }
            out.tags = tags;

            return out;

        } catch (Exception e) {
            System.out.println("[AI] exception=" + e.getClass().getName() + " / " + e.getMessage());
            return out;
        }
    }

    /* =========================
       2) 번역 기능 (저장 X, 화면 표시용)
       - placeName + content 한번에 번역 (추천)
    ========================= */
    public TranslateReviewResponse translateReview(String placeName, String content, String targetLang) {
        TranslateReviewResponse out = new TranslateReviewResponse();
        out.translatedPlaceName = safe(placeName);
        out.translatedContent = safe(content);

        boolean hasKey = hasOpenAiKey();
        System.out.println("[AI] OPENAI KEY EXISTS? " + hasKey);
        if (!hasKey) return out;

        String lang = safe(targetLang);
        if (lang.isBlank()) lang = "en"; // 기본 영어

        String prompt = """
너는 번역기다.
아래 입력을 targetLang 언어로 자연스럽게 번역해라.
- 원문의 의미를 유지
- 고유명사/브랜드명은 가능한 유지
- 결과는 반드시 JSON만 출력 (설명 금지)

[출력 JSON 형식]
{
  "placeName": "...",
  "content": "..."
}

[입력]
targetLang: %s
placeName: %s
content: %s
""".formatted(lang, safe(placeName), safe(content));

        try {
            String text = callOpenAi(prompt, true).trim();
            System.out.println("[AI] translate output text=" + text);
            if (text.isBlank()) return out;

            JsonNode parsed = om.readTree(text);
            out.translatedPlaceName = parsed.path("placeName").asText(safe(placeName));
            out.translatedContent = parsed.path("content").asText(safe(content));
            return out;

        } catch (Exception e) {
            System.out.println("[AI] translate exception=" + e.getClass().getName() + " / " + e.getMessage());
            return out;
        }
    }

    /* =========================
       3) 텍스트 단건 번역 (원하면 사용)
    ========================= */
    public String translateText(String text, String targetLang) {
        boolean hasKey = hasOpenAiKey();
        System.out.println("[AI] OPENAI KEY EXISTS? " + hasKey);
        if (!hasKey) return safe(text);

        String lang = safe(targetLang);
        if (lang.isBlank()) lang = "en";

        String prompt = """
너는 번역기다.
아래 텍스트를 targetLang 언어로 자연스럽게 번역해라.
결과는 반드시 JSON만 출력 (설명 금지)

{
  "translatedText": "..."
}

[입력]
targetLang: %s
text: %s
""".formatted(lang, safe(text));

        try {
            String out = callOpenAi(prompt, true).trim();
            if (out.isBlank()) return safe(text);

            JsonNode parsed = om.readTree(out);
            return parsed.path("translatedText").asText(safe(text));
        } catch (Exception e) {
            System.out.println("[AI] translateText exception=" + e.getClass().getName() + " / " + e.getMessage());
            return safe(text);
        }
    }

    /* =========================
       OpenAI Responses API 호출 공통
       - jsonObject=true면 JSON만 출력하도록 강제
    ========================= */
    private String callOpenAi(String prompt, boolean jsonObject) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4.1-mini");
        body.put("input", prompt);

        if (jsonObject) {
            // ✅ JSON 출력 강제
            body.put("text", Map.of("format", Map.of("type", "json_object")));
        }

        String json = om.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Authorization", "Bearer " + openaiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("[AI] OpenAI status=" + resp.statusCode());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) return "";

        JsonNode root = om.readTree(resp.body());
        return extractOutputText(root);
    }

    // ✅ 여기서 "output[].content[].text" 를 제대로 꺼냄
    private static String extractOutputText(JsonNode root) {
        // 1) 혹시 output_text가 있으면 그걸 사용
        String direct = root.path("output_text").asText("");
        if (direct != null && !direct.isBlank()) return direct;

        // 2) output 배열 순회: content.type == "output_text" 인 text 사용
        JsonNode outputArr = root.path("output");
        if (outputArr.isArray()) {
            for (JsonNode out : outputArr) {
                JsonNode contentArr = out.path("content");
                if (!contentArr.isArray()) continue;

                for (JsonNode c : contentArr) {
                    String type = c.path("type").asText("");
                    if ("output_text".equals(type)) {
                        // ✅ text는 문자열임 (text.value 아님)
                        String t = c.path("text").asText("");
                        if (t != null && !t.isBlank()) return t;
                    }
                }
            }
        }
        return "";
    }

    private boolean hasOpenAiKey() {
        return openaiKey != null && !openaiKey.isBlank();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /* =========================
       번역 응답 DTO (저장 X)
    ========================= */
    @Data
    public static class TranslateReviewResponse {
        public String translatedPlaceName;
        public String translatedContent;
    }
}
