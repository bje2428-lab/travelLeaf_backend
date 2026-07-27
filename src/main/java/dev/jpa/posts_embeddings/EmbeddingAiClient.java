package dev.jpa.posts_embeddings;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class EmbeddingAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    public String createEmbedding(String text) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "text-embedding-3-small",
                    "input", text
            );

            String json = webClient.post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 🔥 OpenAI JSON → embedding 배열만 추출
            JsonNode root = objectMapper.readTree(json);
            JsonNode vectorNode = root.get("data").get(0).get("embedding");

            // 배열 → 문자열 "[-0.12, 0.34, ...]"
            return objectMapper.writeValueAsString(vectorNode);

        } catch (Exception e) {
            throw new RuntimeException("Embedding parse failed", e);
        }
    }
}
