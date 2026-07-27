package dev.jpa.checklist_batch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checklist_batch")
public class ChecklistBatchCont {

    @Autowired
    private ChecklistBatchService checklistBatchService;

    /**
     * 1) 여행(batch) 생성
     * POST http://localhost:9100/checklist_batch/create
     */
    @PostMapping("/create")
    public ChecklistBatch create(@RequestBody ChecklistBatchDTO dto) {
        return checklistBatchService.create(dto);
    }

    /**
     * 2) 특정 batch 단건 조회
     * GET http://localhost:9100/checklist_batch/{batchId}
     */
    @GetMapping("/{batchId}")
    public ChecklistBatch findOne(@PathVariable("batchId") Long batchId) {
        return checklistBatchService.findById(batchId);
    }

    /**
     * 3) 특정 사용자의 여행(batch) 목록 조회
     * GET http://localhost:9100/checklist_batch/user/{userNo}
     */
    @GetMapping("/user/{userNo}")
    public List<ChecklistBatch> findByUser(@PathVariable("userNo") Long userNo) {
        return checklistBatchService.findByUser(userNo);
    }

    /**
     *  4) 여행(batch) 삭제
     * DELETE http://localhost:9100/checklist_batch/{batchId}
     */
    @DeleteMapping("/{batchId}")
    public void delete(@PathVariable("batchId") Long batchId) {
        checklistBatchService.deleteBatch(batchId);
    }
}
