package dev.jpa.ai_plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpa.ai_log.AiLogService;
import dev.jpa.ai_request.AiRequest;
import dev.jpa.ai_request.AiRequestDTO;
import dev.jpa.ai_request.AiRequestService;
import dev.jpa.checklist_batch.ChecklistBatch;
import dev.jpa.checklist_batch.ChecklistBatchService;
import dev.jpa.checklist_user.ChecklistUser;
import dev.jpa.checklist_user.ChecklistUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiPlanAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AiRequestService aiRequestService;
    private final AiLogService aiLogService;
    private final ChecklistBatchService checklistBatchService;
    private final ChecklistUserRepository checklistUserRepository;

    public AiPlanAiService(
            AiRequestService aiRequestService,
            AiLogService aiLogService,
            ChecklistBatchService checklistBatchService,
            ChecklistUserRepository checklistUserRepository
    ) {
        this.aiRequestService = aiRequestService;
        this.aiLogService = aiLogService;
        this.checklistBatchService = checklistBatchService;
        this.checklistUserRepository = checklistUserRepository;
    }

    /**
     * PLAN AI 실행 (백엔드 주도)
     */
    public AiPlanRunResponseDTO runPlanAi(Long userNo, Long batchId) {

        /* =========================
           1. 데이터 조회
        ========================= */
        ChecklistBatch batch = checklistBatchService.findById(batchId);
        List<ChecklistUser> checklistUsers =
                checklistUserRepository.findByUserNoAndBatchId(userNo, batchId);

        /* =========================
           2. 프롬프트 생성
        ========================= */
        String prompt = AiPlanPromptBuilder.build(
                batch,
                checklistUsers,
                List.of() // Activity 이름은 기존 방식 유지 가능
        );

        /* =========================
           3. AI_REQUEST 생성
        ========================= */
        AiRequestDTO reqDto = new AiRequestDTO();
        reqDto.setUserNo(userNo);
        reqDto.setAiType("PLAN");
        reqDto.setInputSummary("batchId=" + batchId);

        AiRequest request = aiRequestService.save(reqDto);
        Long requestId = request.getRequestId();

        long startTime = System.currentTimeMillis();

        try {
            /* =========================
               4. OpenAI 호출
            ========================= */
            String rawText = callOpenAiForJson(prompt, 3000, 0.2);
            String resultJson = ensureValidJsonOrRepair(rawText);

            long latencyMs = System.currentTimeMillis() - startTime;
            aiLogService.logSuccess(requestId, latencyMs);

            AiPlanRunResponseDTO res = new AiPlanRunResponseDTO();
            res.setRequestId(requestId);
            res.setResultJson(resultJson);
            return res;

        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            aiLogService.logFail(requestId, latencyMs, e.getMessage());
            throw new RuntimeException("AI PLAN 실행 실패", e);
        }
    }

    /* =========================
       OpenAI 호출부 (기존 유지)
    ========================= */

    private String callOpenAiForJson(String prompt, int maxTokens, double temperature) throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o");
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.put("response_format", Map.of("type", "json_object"));

        body.put("messages", List.of(
                Map.of("role", "system",
                        "content", "반드시 JSON 객체만 출력해라. JSON 이외 텍스트 금지."),
                Map.of("role", "user",
                        "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(OPENAI_URL, entity, String.class);
        JsonNode root = objectMapper.readTree(response);

        String rawText = root.path("choices")
                .get(0).path("message")
                .path("content").asText();

        if (rawText == null || rawText.isBlank()) {
            throw new RuntimeException("AI 응답이 비어 있습니다.");
        }

        return rawText.trim();
    }

    /* =========================
       JSON 보정 로직 (기존 유지)
    ========================= */

    private String ensureValidJsonOrRepair(String rawText) throws Exception {
        String jsonText = stripToJsonObject(rawText);
        JsonNode node = objectMapper.readTree(jsonText);
        return objectMapper.writeValueAsString(node);
    }

    private String stripToJsonObject(String raw) {
        String t = raw.replace("```json", "").replace("```", "").trim();
        int first = t.indexOf('{');
        int last = t.lastIndexOf('}');
        if (first < 0 || last <= first) {
            throw new RuntimeException("유효한 JSON 객체를 찾을 수 없습니다.");
        }
        return t.substring(first, last + 1)
                .replace("\t", " ")
                .replaceAll("\\r?\\n", " ")
                .trim();
    }
}
