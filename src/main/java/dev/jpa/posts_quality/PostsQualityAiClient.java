package dev.jpa.posts_quality;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PostsQualityAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    public String evaluate(String prompt) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4.1-mini");
        body.put("input", prompt);

        // 🔥 NEW API: JSON 강제 출력
        Map<String, Object> text = new HashMap<>();
        Map<String, Object> format = new HashMap<>();
        format.put("type", "json_object");
        text.put("format", format);

        body.put("text", text);

        return webClient.post()
                .uri("/responses")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        res -> res.bodyToMono(String.class)
                                .map(err -> new RuntimeException("OpenAI 4xx: " + err))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        res -> res.bodyToMono(String.class)
                                .map(err -> new RuntimeException("OpenAI 5xx: " + err))
                )
                .bodyToMono(String.class)
                .block();
    }
}
