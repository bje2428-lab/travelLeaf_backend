package dev.jpa.quiz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizAiService {

    @Value("${openai.api.key:}")
    private String openaiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    private boolean hasKey() {
        return openaiKey != null && !openaiKey.isBlank();
    }

    /* =====================================================
       1) 풀이 피드백/복습/추천 이유 생성 (기존 기능 유지)
    ===================================================== */
    public QuizAttemptAiResult buildAttemptAi(
            Quiz quiz,
            int selectedNo,
            boolean correct,
            List<Quiz> recommendCandidates
    ) {
        if (!hasKey()) {
            QuizAttemptAiResult r = new QuizAttemptAiResult();
            r.setCoachText("");
            r.setRecommendQuizIds("");
            r.setRecommendReason("");
            r.setStrengthText(correct ? "정답 흐름이 좋아요." : "");
            r.setImproveText(correct ? "" : "해설의 핵심 문장을 한 번 더 정리해보세요.");
            r.setNextActionText("비슷한 유형 2~3문제 더 풀어보세요.");
            return r;
        }

        List<Quiz> top = (recommendCandidates == null)
                ? List.of()
                : recommendCandidates.stream().limit(10).toList();

        String candidateText = top.stream()
                .map(q -> "- id=" + q.getQuizId() + " | [" + safe(q.getCategory()) + "] " + safe(q.getQuestion()))
                .collect(Collectors.joining("\n"));

        String system =
                "너는 '자전거/여행 퀴즈 학습 코치'다.\n" +
                "사용자가 방금 푼 퀴즈 정보를 보고 아래 6개 필드를 JSON으로 생성해라.\n\n" +
                "규칙:\n" +
                "- 출력은 반드시 JSON만.\n" +
                "- 값이 없으면 빈 문자열 \"\".\n" +
                "- coachText는 5분 복습용: 핵심요약(1~2문장) + 기억팁 + 한줄암기\n" +
                "- strengthText / improveText / nextActionText는 각각 1~2문장\n\n" +
                "반드시 다음 키로 출력:\n" +
                "coachText, recommendQuizIds, recommendReason, strengthText, improveText, nextActionText";

        String user =
                "[풀이한 문제]\n" +
                "quizId: " + quiz.getQuizId() + "\n" +
                "category: " + safe(quiz.getCategory()) + "\n" +
                "question: " + safe(quiz.getQuestion()) + "\n" +
                "option1: " + safe(quiz.getOption1()) + "\n" +
                "option2: " + safe(quiz.getOption2()) + "\n" +
                "option3: " + safe(quiz.getOption3()) + "\n" +
                "option4: " + safe(quiz.getOption4()) + "\n" +
                "correctNo: " + quiz.getCorrectNo() + "\n" +
                "selectedNo: " + selectedNo + "\n" +
                "isCorrect: " + (correct ? "Y" : "N") + "\n" +
                "explanation: " + safe(quiz.getExplanation()) + "\n\n" +
                "[참고 후보 문제 리스트]\n" +
                (candidateText.isBlank() ? "(없음)" : candidateText);

        try {
            ObjectNode root = om.createObjectNode();
            root.put("model", model);

            ArrayNode input = root.putArray("input");

            ObjectNode s = input.addObject();
            s.put("role", "system");
            s.put("content", system);

            ObjectNode u = input.addObject();
            u.put("role", "user");
            u.put("content", user);

            String body = om.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + openaiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) return emptyAttemptAi();

            JsonNode respRoot = om.readTree(resp.body());
            String outText = extractOutputText(respRoot);
            if (outText == null || outText.isBlank()) return emptyAttemptAi();

            String json = pickJsonObject(outText);
            JsonNode j = om.readTree(json);

            QuizAttemptAiResult r = new QuizAttemptAiResult();
            r.setCoachText(j.path("coachText").asText(""));
            r.setRecommendQuizIds(j.path("recommendQuizIds").asText(""));
            r.setRecommendReason(j.path("recommendReason").asText(""));
            r.setStrengthText(j.path("strengthText").asText(""));
            r.setImproveText(j.path("improveText").asText(""));
            r.setNextActionText(j.path("nextActionText").asText(""));
            return r;

        } catch (Exception e) {
            return emptyAttemptAi();
        }
    }

    private QuizAttemptAiResult emptyAttemptAi() {
        QuizAttemptAiResult r = new QuizAttemptAiResult();
        r.setCoachText("");
        r.setRecommendQuizIds("");
        r.setRecommendReason("");
        r.setStrengthText("");
        r.setImproveText("");
        r.setNextActionText("");
        return r;
    }

    /* =====================================================
       2) ✅ 응용 문제 생성 (핵심)
       - 새 문제 3개를 JSON 배열로 생성해서 PracticeQuizDTO 리스트로 반환
    ===================================================== */
    public List<PracticeQuizDTO> generatePracticeQuizzes(Quiz baseQuiz, boolean correct, int selectedNo) {

        if (!hasKey()) return Collections.emptyList();

        String system =
                "너는 '퀴즈 출제자'다.\n" +
                "아래 기준으로 '응용 문제' 3개를 만들어 JSON 배열만 출력해라.\n\n" +
                "규칙:\n" +
                "- 출력은 반드시 JSON 배열만. (예: [{...},{...},{...}])\n" +
                "- 각 원소는 다음 키를 반드시 포함: question, option1, option2, option3, option4, correctNo, explanation\n" +
                "- correctNo는 1~4 정수\n" +
                "- 보기(option1~4)는 서로 다른 내용\n" +
                "- 난이도는 원문제와 비슷하거나 약간 상향\n" +
                "- 자전거/여행/안전 컨텍스트 유지\n" +
                "- 욕설/비방/차별 표현 금지";

        String user =
                "[원문제]\n" +
                "category: " + safe(baseQuiz.getCategory()) + "\n" +
                "question: " + safe(baseQuiz.getQuestion()) + "\n" +
                "option1: " + safe(baseQuiz.getOption1()) + "\n" +
                "option2: " + safe(baseQuiz.getOption2()) + "\n" +
                "option3: " + safe(baseQuiz.getOption3()) + "\n" +
                "option4: " + safe(baseQuiz.getOption4()) + "\n" +
                "correctNo: " + baseQuiz.getCorrectNo() + "\n" +
                "explanation: " + safe(baseQuiz.getExplanation()) + "\n\n" +
                "[사용자 풀이]\n" +
                "selectedNo: " + selectedNo + "\n" +
                "isCorrect: " + (correct ? "Y" : "N") + "\n\n" +
                "요청: 원문제를 그대로 바꾸지 말고, '상황/조건/수치/규칙'을 변형한 응용문제 3개를 만들어라.";

        try {
            ObjectNode root = om.createObjectNode();
            root.put("model", model);

            ArrayNode input = root.putArray("input");

            ObjectNode s = input.addObject();
            s.put("role", "system");
            s.put("content", system);

            ObjectNode u = input.addObject();
            u.put("role", "user");
            u.put("content", user);

            String body = om.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + openaiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) return Collections.emptyList();

            JsonNode respRoot = om.readTree(resp.body());
            String outText = extractOutputText(respRoot);
            if (outText == null || outText.isBlank()) return Collections.emptyList();

            // 배열 JSON만 나오게 했는데, 혹시 잡텍스트가 섞이면 [] 부분만 잘라서 처리
            String jsonArr = pickJsonArray(outText);

            return om.readValue(jsonArr, new TypeReference<List<PracticeQuizDTO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private String extractOutputText(JsonNode root) {
        JsonNode output = root.path("output");
        if (!output.isArray()) return null;

        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) continue;

            JsonNode content = item.path("content");
            if (!content.isArray()) continue;

            for (JsonNode c : content) {
                if ("output_text".equals(c.path("type").asText())) {
                    return c.path("text").asText(null);
                }
            }
        }
        return null;
    }

    private String pickJsonObject(String text) {
        int a = text.indexOf('{');
        int b = text.lastIndexOf('}');
        if (a >= 0 && b > a) return text.substring(a, b + 1);
        return text;
    }

    private String pickJsonArray(String text) {
        int a = text.indexOf('[');
        int b = text.lastIndexOf(']');
        if (a >= 0 && b > a) return text.substring(a, b + 1);
        return text;
    }
}
