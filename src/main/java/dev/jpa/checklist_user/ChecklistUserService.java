package dev.jpa.checklist_user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.checklist.Checklist;
import dev.jpa.checklist.ChecklistService;
import jakarta.transaction.Transactional;

@Service
public class ChecklistUserService {

    @Autowired
    private ChecklistUserRepository checklistUserRepository;

    /** 🔥 마스터(체크리스트) 서비스 재사용 */
    @Autowired
    private ChecklistService checklistService;

    public ChecklistUserService() {
        System.out.println("-> ChecklistUserService created");
    }

    /**
     * 1) 체크 등록
     * - batchId는 CHECKLIST_BATCH에서 이미 생성된 값
     * - 여기서는 검증 + 저장만 수행
     */
    public ChecklistUser save(ChecklistUserDTO dto) {

        if (dto == null) throw new IllegalArgumentException("요청 DTO가 null 입니다.");
        if (dto.getUserNo() == null) throw new IllegalArgumentException("userNo가 없습니다.");
        if (dto.getItemId() == null) throw new IllegalArgumentException("itemId가 없습니다.");
        if (dto.getBatchId() == null) throw new IllegalArgumentException("batchId가 없습니다. (CHECKLIST_BATCH 기준)");

        return checklistUserRepository.save(dto.toEntity());
    }

    /**
     * 2) 특정 사용자 + 특정 여행(BATCH_ID)의 체크 목록 조회 (원본)
     * - CHECKLIST_USER 엔티티 그대로 반환 (itemId만 있음)
     */
    public List<ChecklistUser> findByUserNoAndBatchId(Long userNo, Long batchId) {
        return checklistUserRepository.findByUserNoAndBatchIdOrderByCheckIdAsc(userNo, batchId);
    }

    /**
     * 🔥 2-1) 디테일 화면용 (마스터 형태로 반환)
     * - DTO/Projection 안 만들고
     * - 기존 Checklist 엔티티 그대로 반환
     *
     * 프런트에서 예전처럼 item.itemName / item.description / item.category 그대로 사용 가능
     */
    public List<Checklist> findDetailItemsAsMaster(Long userNo, Long batchId) {

        // 1) 선택내역(정렬된) 조회
        List<ChecklistUser> selected = checklistUserRepository
                .findByUserNoAndBatchIdOrderByCheckIdAsc(userNo, batchId);

        // 2) itemId 리스트로 마스터 조회
        //    (팀플/개인프로젝트 규모면 N번 조회도 현실적으로 문제 없음)
        List<Checklist> result = new ArrayList<>();
        for (ChecklistUser cu : selected) {
            Checklist item = checklistService.findByItemId(cu.getItemId()); // 너가 이미 쓰던 서비스 메서드
            if (item != null) result.add(item);
        }
        return result;
    }

    /**
     * 3) PK 단건 조회
     */
    public Optional<ChecklistUser> findById(Long checkId) {
        return checklistUserRepository.findById(checkId);
    }

    /**
     * 4) 특정 여행에서 특정 항목 선택 여부 확인
     */
    public boolean exists(Long userNo, Long batchId, Long itemId) {
        return checklistUserRepository.existsByUserNoAndBatchIdAndItemId(userNo, batchId, itemId);
    }

    /**
     * 5) 체크 해제 (낱개 삭제)
     */
    @Transactional
    public void delete(Long userNo, Long batchId, Long itemId) {
        checklistUserRepository.deleteByUserNoAndBatchIdAndItemId(userNo, batchId, itemId);
    }

    /**
     * 6) 특정 사용자 + 특정 여행의 전체 체크 삭제
     */
    @Transactional
    public void deleteAllByUserAndBatch(Long userNo, Long batchId) {
        checklistUserRepository.deleteAllByUserNoAndBatchId(userNo, batchId);
    }

    /**
     * 7) 토글 기능
     */
    @Transactional
    public String toggle(Long userNo, Long batchId, Long itemId) {

        boolean exists = checklistUserRepository.existsByUserNoAndBatchIdAndItemId(userNo, batchId, itemId);

        if (exists) {
            checklistUserRepository.deleteByUserNoAndBatchIdAndItemId(userNo, batchId, itemId);
            return "unchecked";
        }

        checklistUserRepository.save(new ChecklistUser(userNo, itemId, batchId));
        return "checked";
    }

    /**
     * 8) 관리자용 — 특정 itemId가 선택된 전체 데이터 조회
     */
    public List<ChecklistUser> findByItemId(Long itemId) {
        return checklistUserRepository.findByItemId(itemId);
    }

    /**
     * 9) 관리자용 — 전체 체크리스트 데이터 조회
     */
    public List<ChecklistUser> findAll() {
        return checklistUserRepository.findAllByOrderByCheckIdAsc();
    }

    /**
     * 10) 체크 항목 수정
     */
    @Transactional
    public ChecklistUser update(Long checkId, ChecklistUserDTO dto) {

        ChecklistUser entity = checklistUserRepository.findById(checkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 checkId 입니다."));

        if (dto.getUserNo() != null) entity.setUserNo(dto.getUserNo());
        if (dto.getItemId() != null) entity.setItemId(dto.getItemId());
        if (dto.getBatchId() != null) entity.setBatchId(dto.getBatchId());

        return checklistUserRepository.save(entity);
    }
}
