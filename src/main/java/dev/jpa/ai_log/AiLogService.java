package dev.jpa.ai_log;

import dev.jpa.ai_request.AiRequest;
import dev.jpa.ai_request.AiRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiLogService {

    private final AiLogRepository aiLogRepository;
    private final AiRequestRepository aiRequestRepository;

    public AiLogService(
            AiLogRepository aiLogRepository,
            AiRequestRepository aiRequestRepository
    ) {
        this.aiLogRepository = aiLogRepository;
        this.aiRequestRepository = aiRequestRepository;
    }

    /* =========================
       SUCCESS 로그 저장 (변경 없음)
    ========================= */
    @Transactional
    public void logSuccess(Long requestId, long latencyMs) {
        AiRequest request = aiRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("AI_REQUEST not found: " + requestId));

        AiLog log = new AiLog();
        log.setAiRequest(request);
        log.setStatus("SUCCESS");
        log.setLatencyMs(latencyMs);
        log.setErrorMessage(null);

        aiLogRepository.save(log);
    }

    /* =========================
       FAIL 로그 저장 (변경 없음)
    ========================= */
    @Transactional
    public void logFail(Long requestId, long latencyMs, String errorMessage) {
        AiRequest request = aiRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("AI_REQUEST not found: " + requestId));

        AiLog log = new AiLog();
        log.setAiRequest(request);
        log.setStatus("FAIL");
        log.setLatencyMs(latencyMs);
        log.setErrorMessage(errorMessage);

        aiLogRepository.save(log);
    }

    /* =========================
       로그 페이지 조회 (Repository 변경 반영)
    ========================= */
    @Transactional(readOnly = true)
    public Page<AiLog> getLogPage(int page, int size, String status) {

        // page는 1부터 들어오므로 -1 처리
        Pageable pageable = PageRequest.of(page - 1, size);

        if (status == null || status.isBlank()) {
            return aiLogRepository.findAllByOrderByLogIdDesc(pageable);
        }

        return aiLogRepository.findByStatusOrderByLogIdDesc(status, pageable);
    }
}
