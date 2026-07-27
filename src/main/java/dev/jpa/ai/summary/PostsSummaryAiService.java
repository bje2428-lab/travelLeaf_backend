package dev.jpa.ai.summary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpa.posts.Posts;
import dev.jpa.posts.PostsRepository;
import dev.jpa.posts_summary.PostsSummary;
import dev.jpa.posts_summary.PostsSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PostsSummaryAiService {

    private final PostsRepository postsRepository;
    private final PostsSummaryRepository summaryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FASTAPI_URL = "http://localhost:11307/summary";

    public void generateSummary(Long postId) {

        // 1. 게시글 조회
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음: " + postId));

        // 2. FastAPI 호출 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 JSON
        String requestJson = String.format("{\"text\": \"%s\\n%s\"}",
                post.getTitle().replace("\"", "\\\""),
                post.getContent().replace("\"", "\\\""));

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        try {
            // 3. FastAPI 호출
            ResponseEntity<String> response = restTemplate.postForEntity(FASTAPI_URL, entity, String.class);

            // 4. 응답 파싱
            JsonNode root = objectMapper.readTree(response.getBody());
            String summaryText = root.path("summary").asText();

            // 5. DB 저장
            PostsSummary ps = new PostsSummary();
            ps.setPostId(postId);
            ps.setSummary(summaryText);

            summaryRepository.save(ps);

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패", e);
        }
    }
}
