package dev.jpa.ai.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpa.comments_reports.CommentsReport;
import dev.jpa.comments_reports.CommentsReportRepository;
import dev.jpa.posts_reports.PostsReport;
import dev.jpa.posts_reports.PostsReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ModerationAiClient aiClient;
    private final PostsReportRepository postsReportRepository;
    private final CommentsReportRepository commentsReportRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double THRESHOLD = 0.8;
    private static final String AI_REPORTER = "user1";   // 🔥 실제 DB 관리자 계정

    /* ==========================
       게시글 자동 신고
    ========================== */
    @Async
    public void analyzePost(Long postId, String content) {
        try {
            AiModerationResult result = analyze(content);

            if (result.getToxicityScore() >= THRESHOLD) {
                PostsReport report = PostsReport.builder()
                        .postId(postId)
                        .reporterId(AI_REPORTER)     // 🔥 FK 통과
                        .reportCategory(result.getReason())
                        .reason("AI 자동 감지")
                        .status("PENDING")           // 🔥 체크제약 통과
                        .aiScore(result.getToxicityScore())
                        .aiModel("gpt-4.1-mini")
                        .aiDetected("Y")
                        .build();

                postsReportRepository.save(report);
            }
        } catch (Exception e) {
            System.out.println("⚠ AI 게시글 분석 실패 (게시글 등록은 유지) postId=" + postId);
            e.printStackTrace();
        }
    }

    /* ==========================
       댓글 자동 신고
    ========================== */
    @Async
    public void analyzeComment(Long commentId, String content) {
        try {
            AiModerationResult result = analyze(content);

            if (result.getToxicityScore() >= THRESHOLD) {
                CommentsReport report = CommentsReport.builder()
                        .commentId(commentId)
                        .reporterId(AI_REPORTER)     // 🔥 FK 통과
                        .reportCategory(result.getReason())
                        .reason("AI 자동 감지")
                        .status("PENDING")           // 🔥 체크제약 통과
                        .aiScore(result.getToxicityScore())
                        .aiModel("gpt-4.1-mini")
                        .aiDetected("Y")
                        .build();

                commentsReportRepository.save(report);
            }
        } catch (Exception e) {
            System.out.println("⚠ AI 댓글 분석 실패 (댓글 등록은 유지) commentId=" + commentId);
            e.printStackTrace();
        }
    }

    /* ==========================
       공통 분석 로직
    ========================== */
    private AiModerationResult analyze(String content) throws Exception {

        String raw = aiClient.analyzeRaw(content);

        JsonNode root = objectMapper.readTree(raw);

        String jsonText = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();

        // ```json 제거
        if (jsonText.startsWith("```")) {
            jsonText = jsonText
                    .replaceAll("(?s)```json", "")
                    .replaceAll("```", "")
                    .trim();
        }

        return objectMapper.readValue(jsonText, AiModerationResult.class);
    }
}
