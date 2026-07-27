package dev.jpa.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ReviewModerationService {

    @Value("${openai.api.key:}")
    private String openaiKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    // ✅ 확실한 욕설/비속어만 1차 룰로 즉시 차단(원하면 더 추가)
    private final List<Pattern> hardBan = List.of(
            Pattern.compile("(?i)(씨발|ㅅㅂ|시발|병신|ㅂㅅ|좆|존나)")
    );

    // ✅ 차단 기준(너 서비스 정책)
    private static final double BLOCK_SCORE = 0.65;

    public AiReviewCheck check(String text) {
        String input = (text == null) ? "" : text.trim();

        AiReviewCheck out = new AiReviewCheck();
        out.allowed = true;
        out.toxicScore = 0.0;
        out.sentiment = "NEU";
        out.flagReason = null;

        if (input.isBlank()) return out;

        // ✅ 키 없으면(개발용) 통과 처리 (원하면 false로 바꿔도 됨)
        if (openaiKey == null || openaiKey.isBlank()) {
            out.allowed = true;
            out.flagReason = "moderation disabled";
            return out;
        }

        // 1) 룰 기반 욕설 즉시 차단
        for (Pattern p : hardBan) {
            if (p.matcher(input).find()) {
                out.allowed = false;
                out.toxicScore = 1.0;
                out.sentiment = "NEG";
                out.flagReason = "SWEAR"; // ✅ 이 값으로 서비스에서 메시지 분기
                return out;
            }
        }

        // 2) Moderations API 점수 계산
        ModerationScores ms = callModeration(input);
        double tox = Math.max(ms.harassment, Math.max(ms.hate, ms.violence));

        out.toxicScore = round2(tox);

        // 3) 감정(톤) 분석
        out.sentiment = callSentimentPOSNEUNEG(input);

        // 4) 차단 여부
        if (ms.flagged || tox >= BLOCK_SCORE) {
            out.allowed = false;
            out.flagReason = "INAPPROPRIATE";
        } else {
            out.allowed = true;
            out.flagReason = "OK";
        }

        return out;
    }

    private ModerationScores callModeration(String input) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "omni-moderation-latest");
            body.put("input", input);

            String json = om.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/moderations"))
                    .header("Authorization", "Bearer " + openaiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return ModerationScores.safePass();
            }

            JsonNode root = om.readTree(resp.body());
            JsonNode r0 = root.path("results").get(0);

            boolean flagged = r0.path("flagged").asBoolean(false);
            JsonNode scores = r0.path("category_scores");

            ModerationScores ms = new ModerationScores();
            ms.flagged = flagged;
            ms.harassment = scores.path("harassment").asDouble(0);
            ms.hate = scores.path("hate").asDouble(0);
            ms.violence = scores.path("violence").asDouble(0);

            return ms;

        } catch (Exception e) {
            return ModerationScores.safePass();
        }
    }

    private String callSentimentPOSNEUNEG(String text) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4.1-mini");
            body.put("input",
                    "다음 문장의 감정/톤을 POS/NEU/NEG 중 하나로만 답해. " +
                    "출력은 딱 한 단어(POS 또는 NEU 또는 NEG)만.\n\n" + text
            );

            String json = om.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + openaiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) return "NEU";

            JsonNode root = om.readTree(resp.body());
            String out = root.path("output_text").asText("").trim().toUpperCase(Locale.ROOT);

            if (out.equals("POS") || out.equals("NEU") || out.equals("NEG")) return out;

            // fallback
            JsonNode output = root.path("output");
            if (output.isArray()) {
                for (JsonNode item : output) {
                    JsonNode content = item.path("content");
                    if (content.isArray() && content.size() > 0) {
                        String t = content.get(0).path("text").asText("").trim().toUpperCase(Locale.ROOT);
                        if (t.equals("POS") || t.equals("NEU") || t.equals("NEG")) return t;
                    }
                }
            }
            return "NEU";
        } catch (Exception e) {
            return "NEU";
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    static class ModerationScores {
        boolean flagged;
        double harassment;
        double hate;
        double violence;

        static ModerationScores safePass() {
            ModerationScores ms = new ModerationScores();
            ms.flagged = false;
            ms.harassment = 0;
            ms.hate = 0;
            ms.violence = 0;
            return ms;
        }
    }
}
