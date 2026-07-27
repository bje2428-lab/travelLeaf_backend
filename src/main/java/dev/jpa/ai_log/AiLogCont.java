package dev.jpa.ai_log;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai_log")
public class AiLogCont {

    private final AiLogService aiLogService;

    public AiLogCont(AiLogService aiLogService) {
        this.aiLogService = aiLogService;
    }

    /**
     * AI 로그 페이지 조회
     *
     * GET /ai_log/list?page=1&size=20
     * GET /ai_log/list?page=1&size=20&status=FAIL
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", defaultValue = "") String status
    ) {
        Page<AiLog> result = aiLogService.getLogPage(
                page,
                size,
                status.isBlank() ? null : status
        );

        return Map.of(
                "content", result.getContent().stream()
                        .map(AiLogDTO::from)
                        .toList(),
                "page", page,
                "size", size,
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }
}
