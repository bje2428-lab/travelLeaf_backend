package dev.jpa.posts_quality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpa.posts.Posts;
import dev.jpa.posts.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostsQualityService {

    private final PostsRepository postsRepository;
    private final PostsQualityRepository qualityRepository;
    private final PostsQualityAiClient aiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void analyzePost(Long postId) {

        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 🔥 후하게 + 스팸만 잡는 프롬프트
        String prompt = """
            다음 게시글을 평가하라.

            목적:
            - 일반적인 커뮤니티 글은 모두 정상으로 간주한다.
            - 오직 명백한 광고, 사기, 링크도배, 욕설, 불법, 음란, 낚시글만 스팸으로 판단한다.
            - 조금 홍보 같거나, 링크가 하나 있어도 스팸으로 보지 않는다.

            평가 기준:
            - readability (0~100)
            - originality (0~100)
            - usefulness (0~100)

            spamScore (0~100):
            - 0~20: 정상
            - 21~40: 약간 의심
            - 41~60: 광고 의심
            - 61~80: 명백한 스팸
            - 81~100: 극단적인 스팸

            반드시 JSON만 출력하라:

            {
              "readability": number,
              "originality": number,
              "usefulness": number,
              "spamScore": number
            }

            제목: %s
            내용:
            %s
            """.formatted(post.getTitle(), post.getContent());


        String response = aiClient.evaluate(prompt);

        try {
            JsonNode root = objectMapper.readTree(response);

            String content = root
                    .path("output").path(0)
                    .path("content").path(0)
                    .path("text")
                    .asText()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode json = objectMapper.readTree(content);

            double readability = json.path("readability").asDouble(50);
            double originality = json.path("originality").asDouble(50);
            double usefulness = json.path("usefulness").asDouble(50);
            double spamScore = json.path("spamScore").asDouble(0);

            // 🔥 기본 점수 (정상글은 대부분 30~70)
            double aiScore =
                    readability * 0.3 +
                    originality * 0.4 +
                    usefulness * 0.3;

            // 🔥 스팸 패널티
            if (spamScore >= 80) {
                aiScore *= 0.1;   // 거의 스팸
            } else if (spamScore >= 60) {
                aiScore *= 0.4;
            } else if (spamScore >= 40) {
                aiScore *= 0.7;
            }

            // 🔥 바닥값 보정 (0점 방지)
            if (aiScore < 1 && spamScore < 60) {
                aiScore = 5; // 정상 글인데 AI가 낮게 준 경우 보정
            }

            // 🔥 qualityGrade는 서버에서 계산
            String qualityGrade;
            if (spamScore >= 80 || aiScore < 1) {
                qualityGrade = "SPAM";
            } else {
                qualityGrade = "NORMAL";
            }

            qualityRepository.upsertQuality(
                    postId,
                    readability,
                    originality,
                    usefulness,
                    aiScore,
                    spamScore,
                    qualityGrade
            );

        } catch (Exception e) {
            throw new RuntimeException("AI JSON parse failed. Raw response:\n" + response, e);
        }
    }

    public QualityScoreDTO getScore(Long postId) {

        return qualityRepository.findById(postId)
                .map(q -> {
                    QualityScoreDTO dto = new QualityScoreDTO();
                    dto.setReadability(q.getReadability());
                    dto.setOriginality(q.getOriginality());
                    dto.setUsefulness(q.getUsefulness());
                    dto.setAiScore(q.getAiScore());
                    dto.setSpamScore(q.getSpamScore());
                    dto.setQualityGrade(q.getQualityGrade());
                    return dto;
                })
                .orElseGet(() -> {
                    QualityScoreDTO dto = new QualityScoreDTO();
                    dto.setAiScore(0.0);
                    dto.setSpamScore(0.0);
                    dto.setQualityGrade("NOT_ANALYZED");
                    return dto;
                });
    }
}
