package dev.jpa.ai.summary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SummaryAiClient {

    private static final String FASTAPI_URL = "http://localhost:11307/summary"; // 환경에 맞게 수정
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String requestSummary(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // JSON 요청 안전하게 직렬화
            String requestJson = objectMapper.writeValueAsString(Map.of("text", prompt));

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(FASTAPI_URL, entity, String.class);

            // 상태 코드 체크
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("FastAPI 호출 실패: HTTP 상태 " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            String summary = root.path("summary").asText(null);

            if (summary == null || summary.isEmpty()) {
                throw new RuntimeException("FastAPI 요약 결과 없음");
            }

            return summary;

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패", e);
        }
    }
}