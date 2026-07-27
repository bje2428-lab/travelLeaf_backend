package dev.jpa.ai_request;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AiRequestService {

    private final AiRequestRepository aiRequestRepository;

    public AiRequestService(AiRequestRepository aiRequestRepository) {
        this.aiRequestRepository = aiRequestRepository;
        System.out.println("-> AiRequestService created");
    }

    /**
     * ✅ AI 요청 로그 저장
     * - 중복 검사 ❌
     * - 항상 새 row 생성
     */
    @Transactional
    public AiRequest save(AiRequestDTO dto) {

        AiRequest entity = new AiRequest();
        entity.setUserNo(dto.getUserNo());
        entity.setAiType(dto.getAiType());
        entity.setInputSummary(dto.getInputSummary());
        entity.setCreatedAt(LocalDateTime.now());

        return aiRequestRepository.save(entity);
    }
}
