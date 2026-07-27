package dev.jpa.ai.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ModerationAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();

    public String analyzeRaw(String content) {

        String body = """
        {
          "model": "gpt-4.1-mini",
          "messages": [
            {
              "role": "system",
              "content": "You are a content moderation AI. Return JSON only."
            },
            {
              "role": "user",
              "content": "Analyze the following text. Return JSON with fields: toxicity_score (0~1) and reason (INSULT, HATE, AD, SPAM, SEXUAL, THREAT, NORMAL). Text: %s"
            }
          ]
        }
        """.formatted(content.replace("\"", "'"));

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
