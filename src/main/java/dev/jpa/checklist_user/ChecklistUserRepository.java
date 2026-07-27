package dev.jpa.checklist_user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ChecklistUserRepository extends JpaRepository<ChecklistUser, Long> {

    /** 
     * 1) 특정 사용자 + 특정 여행(BATCH_ID)의 체크리스트 전체 조회
     */
    List<ChecklistUser> findByUserNoAndBatchId(Long userNo, Long batchId);

    /** 
     *  디테일용: CHECK_ID 기준 정렬된 조회
     * - Detail 화면에서 선택 순서 유지용
     */
    List<ChecklistUser> findByUserNoAndBatchIdOrderByCheckIdAsc(Long userNo, Long batchId);

    /**
     * 2) 특정 사용자 + 특정 여행에서 특정 체크리스트 항목 선택 여부
     */
    boolean existsByUserNoAndBatchIdAndItemId(
            Long userNo,
            Long batchId,
            Long itemId
    );

    /**
     * 3) 특정 사용자 + 특정 여행에서 특정 체크리스트 항목 선택 해제
     * - 낱개 삭제
     */
    @Modifying
    @Transactional
    void deleteByUserNoAndBatchIdAndItemId(
            Long userNo,
            Long batchId,
            Long itemId
    );

    /**
     * 4) 관리자용: 전체 체크 데이터 조회 (PK 기준 정렬)
     */
    List<ChecklistUser> findAllByOrderByCheckIdAsc();

    /**
     * 5) 관리자용: 특정 체크리스트 항목이 어떤 여행들에서 선택됐는지 조회
     */
    List<ChecklistUser> findByItemId(Long itemId);

    /**
     * 6) 특정 사용자 + 특정 여행의 체크리스트 전체 삭제
     * - 기존 프런트/서비스에서 사용
     */
    @Modifying
    @Transactional
    void deleteAllByUserNoAndBatchId(Long userNo, Long batchId);

    /**
     *  7) 특정 여행(BATCH_ID)에 속한 체크리스트 전체 삭제
     * - 여행(batch) 삭제 시 사용
     * - userNo와 무관하게 batch 기준으로 정리
     */
    @Modifying
    @Transactional
    void deleteAllByBatchId(Long batchId);
}
