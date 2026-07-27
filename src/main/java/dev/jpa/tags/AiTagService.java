package dev.jpa.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.tags.dto.AiTagRequest;
import dev.jpa.tags.dto.AiTagResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiTagService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * AI 태그 생성 (제목 + 내용 + 기존 태그 참고)
     */
    public AiTagResponse generateTags(AiTagRequest req) {

        String prompt = buildPrompt(
                req.getTitle(),
                req.getContent(),
                req.getTags()
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "너는 한국어 게시글을 분석해서 태그를 추천하는 AI다."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        var entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        Map response = restTemplate.postForObject(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
        );

        String content =
                (String) ((Map) ((Map) ((List) response.get("choices")).get(0))
                        .get("message")).get("content");

        // AI 응답 → 태그 리스트 변환
        Set<String> mergedTags = mergeTags(
                req.getTags(),
                parseTags(content)
        );

        return new AiTagResponse(new ArrayList<>(mergedTags));
    }

    /**
     * 프롬프트 생성
     */
    private String buildPrompt(String title, String content, List<String> userTags) {

        return """
                다음 게시글을 분석해서 태그를 추천해줘.

                [제목]
                %s

                [내용]
                %s

                [사용자가 입력한 태그]
                %s

                규칙:
                - 태그는 3~7개
                - 모두 한글
                - 너무 일반적인 단어 제외
                - 이미 있는 태그는 유지
                - 새로운 태그만 보완
                - 결과는 쉼표(,)로만 구분해서 출력
                """.formatted(
                title,
                content,
                userTags == null ? "" : String.join(", ", userTags)
        );
    }

    /**
     * AI 응답 파싱
     */
    private List<String> parseTags(String aiText) {
        return Arrays.stream(aiText.split(","))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * 기존 태그 + AI 태그 병합 (중복 제거)
     */
    private Set<String> mergeTags(List<String> userTags, List<String> aiTags) {
        Set<String> result = new LinkedHashSet<>();

        if (userTags != null) {
            result.addAll(
                    userTags.stream()
                            .map(String::trim)
                            .filter(t -> !t.isBlank())
                            .toList()
            );
        }

        result.addAll(aiTags);

        return result;
    }
}
