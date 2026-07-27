package dev.jpa.ai_weather_core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiWeatherAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 날씨 분석 프롬프트를 GPT에 전달하여
     * 사용자 안내 메시지를 생성한다.
     */
    public String generateMessage(String prompt) {

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o-mini");

            // 핵심 튜닝
            body.put("temperature", 0.2);   // 조건 차별화 강화
            body.put("max_tokens", 500);    // 지침 잘리기 방지

            body.put("messages", List.of(
                    Map.of(
                            "role", "system",
                            "content",
                            """
                            너는 자전거 여행 일정의 '특정 날짜·특정 지점'을 기준으로
                            기상 조건을 해석하는 안전 전문가다.

                            이 분석은 일반적인 하루 요약이 아니라,
                            해당 지점에서 실제로 라이딩하거나 체류할 때의
                            위험 요소와 행동 지침을 안내하는 것이 목적이다.

                            조건이 달라지면 반드시 다른 표현과 다른 조언을 사용하라.
                            """
                    ),
                    Map.of(
                            "role", "user",
                            "content",
                            prompt + """

                            [출력 형식]
                            1. 주의사항:
                            2. 권장 라이딩 방식/시간:
                            3. 일정 조정 또는 우회 제안:
                            4. 실전 라이딩 팁:

                            각 항목은 1~2문장으로 작성하라.
                            """
                    )
            ));

            org.springframework.http.HttpHeaders headers =
                    new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(body, headers);

            String response =
                    restTemplate.postForObject(
                            OPENAI_URL,
                            entity,
                            String.class
                    );

            JsonNode root = objectMapper.readTree(response);

            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

        } catch (Exception e) {
            throw new RuntimeException("AI 날씨 메시지 생성 실패", e);
        }
    }
}
