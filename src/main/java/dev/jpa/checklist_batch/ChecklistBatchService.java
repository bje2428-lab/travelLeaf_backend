package dev.jpa.checklist_batch;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.ai_plan.AiPlanRepository;
import dev.jpa.checklist_user.ChecklistUserRepository;

@Service
@Transactional
public class ChecklistBatchService {

    private final ChecklistBatchRepository checklistBatchRepository;
    private final ChecklistUserRepository checklistUserRepository;
    private final AiPlanRepository aiPlanRepository;

    public ChecklistBatchService(
            ChecklistBatchRepository checklistBatchRepository,
            ChecklistUserRepository checklistUserRepository,
            AiPlanRepository aiPlanRepository
    ) {
        this.checklistBatchRepository = checklistBatchRepository;
        this.checklistUserRepository = checklistUserRepository;
        this.aiPlanRepository = aiPlanRepository;
    }

    /* =========================
       생성
    ========================= */
    public ChecklistBatch create(ChecklistBatchDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("ChecklistBatchDTO가 null 입니다.");
        }
        if (dto.getUserNo() == null) {
            throw new IllegalArgumentException("userNo는 필수입니다.");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("title은 필수입니다.");
        }
        if (dto.getRouteRegions() == null || dto.getRouteRegions().trim().isEmpty()) {
            throw new IllegalArgumentException("routeRegions는 필수입니다.");
        }
        if (dto.getStartDatetime() == null || dto.getEndDatetime() == null) {
            throw new IllegalArgumentException("여행 시작/종료 일시는 필수입니다.");
        }

        if (dto.getStartDatetime().length() != 16 ||
            dto.getEndDatetime().length() != 16) {
            throw new IllegalArgumentException(
                "날짜 형식은 yyyy-MM-ddTHH:mm 이어야 합니다."
            );
        }

        ChecklistBatch batch = new ChecklistBatch();
        batch.setUserNo(dto.getUserNo());
        batch.setTitle(dto.getTitle().trim());
        batch.setRouteRegions(dto.getRouteRegions());
        batch.setRouteCities(dto.getRouteCities());
        batch.setRouteWaypoints(dto.getRouteWaypoints());
        batch.setStartPoint(dto.getStartPoint());
        batch.setEndPoint(dto.getEndPoint());
        batch.setStartDatetime(dto.getStartDatetime());
        batch.setEndDatetime(dto.getEndDatetime());

        return checklistBatchRepository.save(batch);
    }

    /* =========================
       조회
    ========================= */
    @Transactional(readOnly = true)
    public ChecklistBatch findById(Long batchId) {
        return checklistBatchRepository.findById(batchId)
            .orElseThrow(() ->
                new IllegalArgumentException("ChecklistBatch not found. batchId=" + batchId));
    }

    @Transactional(readOnly = true)
    public List<ChecklistBatch> findByUser(Long userNo) {
        return checklistBatchRepository.findByUserNoOrderByBatchIdDesc(userNo);
    }

    /* =========================
       삭제 
    ========================= */
    public void deleteBatch(Long batchId) {
        aiPlanRepository.deleteAllByBatchId(batchId);
        checklistUserRepository.deleteAllByBatchId(batchId);
        checklistBatchRepository.deleteById(batchId);
    }
}
