package dev.jpa.ai_plan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface AiPlanRepository extends JpaRepository<AiPlan, Long> {

    /**
     * 1) 특정 체크리스트 저장본(batchId)에 대한
     *    AI 여행 일정 초안 전체 조회
     */
    List<AiPlan> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    /**
     * 2) 특정 체크리스트 저장본(batchId)에 대한
     *    가장 최근 AI 여행 일정 초안 1건 조회
     *    (AI 재추천 시 사용)
     */
    Optional<AiPlan> findTopByBatchIdOrderByCreatedAtDesc(Long batchId);

    /**
     * 3) 특정 사용자가 생성한 AI 여행 일정 초안 전체 조회
     *    (마이페이지 / 히스토리 용)
     */
    List<AiPlan> findByUserNoOrderByCreatedAtDesc(Long userNo);

    /**
     * 4) 관리자용 — 전체 AI 여행 일정 초안 조회
     */
    List<AiPlan> findAllByOrderByCreatedAtDesc();

    /**
     * 5) 페이징
     */
    Page<AiPlan> findByBatchIdAndUserNo(
        Long batchId,
        Long userNo,
        Pageable pageable
    );
    
    /**
     * 6) 삭제
     */
    @Modifying
    @Transactional
    void deleteAllByBatchId(Long batchId);

}
