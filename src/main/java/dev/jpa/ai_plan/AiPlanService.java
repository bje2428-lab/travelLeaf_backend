package dev.jpa.ai_plan;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🔥 이걸로 통일

@Service
public class AiPlanService {

    @Autowired
    private AiPlanRepository aiPlanRepository;

    public AiPlanService() {
        System.out.println("-> AiPlanService created");
    }

    /* ==================================================
       1) AI 여행 일정 결과 저장
    ================================================== */
    @Transactional
    public AiPlan save(AiPlanDTO dto) {
        return aiPlanRepository.save(dto.toEntity());
    }

    /* ==================================================
       2) 특정 batchId 전체 조회
    ================================================== */
    @Transactional(readOnly = true)
    public List<AiPlan> findByBatchId(Long batchId) {
        return aiPlanRepository.findByBatchIdOrderByCreatedAtDesc(batchId);
    }

    /* ==================================================
       3) 최신 1건 조회
    ================================================== */
    @Transactional(readOnly = true)
    public Optional<AiPlan> findLatestByBatchId(Long batchId) {
        return aiPlanRepository.findTopByBatchIdOrderByCreatedAtDesc(batchId);
    }

    /* ==================================================
       4) 사용자 기준 조회
    ================================================== */
    @Transactional(readOnly = true)
    public List<AiPlan> findByUserNo(Long userNo) {
        return aiPlanRepository.findByUserNoOrderByCreatedAtDesc(userNo);
    }

    /* ==================================================
       5) PK 단건 조회
    ================================================== */
    @Transactional(readOnly = true)
    public Optional<AiPlan> findById(Long aiPlanId) {
        return aiPlanRepository.findById(aiPlanId);
    }

    /* ==================================================
       6) 관리자 전체 조회
    ================================================== */
    @Transactional(readOnly = true)
    public List<AiPlan> findAll() {
        return aiPlanRepository.findAllByOrderByCreatedAtDesc();
    }

    /* ==================================================
       7) 페이징 조회
    ================================================== */
    @Transactional(readOnly = true)
    public Page<AiPlan> findByBatchAndUser(
            Long batchId,
            Long userNo,
            Pageable pageable
    ) {
        return aiPlanRepository.findByBatchIdAndUserNo(
                batchId,
                userNo,
                pageable
        );
    }
}
