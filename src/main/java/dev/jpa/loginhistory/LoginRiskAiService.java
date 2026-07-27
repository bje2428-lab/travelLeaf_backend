package dev.jpa.loginhistory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class LoginRiskAiService {

    @Value("${openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateRiskReason(
            String usualTime,
            String currentTime,
            boolean ipChanged,
            boolean agentChanged
    ) {

        String prompt = """
        아래는 한 사용자의 로그인 분석 정보이다.

        - 주 로그인 시간대: %s
        - IP 변경 여부: %s
        - 디바이스/브라우저 변경 여부: %s

        이 로그인 시도가 왜 의심되는지
        관리자에게 보여줄 설명 문장을 2문장 이내로 작성해줘.
        """.formatted(
                usualTime,
                currentTime,
                ipChanged ? "변경됨" : "변경 없음",
                agentChanged ? "변경됨" : "변경 없음"
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        body.put("temperature", 0.3);

        var headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        var request = new org.springframework.http.HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
        );

        List choices = (List) response.get("choices");
        Map message = (Map) ((Map) choices.get(0)).get("message");

        return message.get("content").toString();
    }
}
