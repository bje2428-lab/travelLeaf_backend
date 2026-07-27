package dev.jpa.ai_log;

import lombok.Getter;
import java.util.Date;

@Getter
public class AiLogDTO {

    private Long logId;
    private Long requestId;
    private String aiType;
    private String status;
    private Long latencyMs;
    private String errorMessage;
    private Date createdAt;

    public static AiLogDTO from(AiLog log) {
        AiLogDTO dto = new AiLogDTO();

        dto.logId = log.getLogId();
        dto.requestId = log.getAiRequest().getRequestId();

        // AI_TYPE 가져오기
        dto.aiType = log.getAiRequest().getAiType();

        dto.status = log.getStatus();
        dto.latencyMs = log.getLatencyMs();
        dto.errorMessage = log.getErrorMessage();
        dto.createdAt = log.getCreatedAt();

        return dto;
    }
}
