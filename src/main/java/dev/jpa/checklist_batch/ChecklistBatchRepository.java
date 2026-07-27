package dev.jpa.checklist_batch;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistBatchRepository
        extends JpaRepository<ChecklistBatch, Long> {

    /**
     * 특정 사용자의 여행(batch) 목록 조회
     * - 최신 여행이 위로 오도록 정렬
     */
    List<ChecklistBatch> findByUserNoOrderByBatchIdDesc(Long userNo);

    /**
     *  batchId 기준 여행 삭제
     * (JpaRepository 기본 deleteById 사용 가능하지만
     *  의미를 명확히 하기 위해 선언)
     */
    void deleteByBatchId(Long batchId);
}
