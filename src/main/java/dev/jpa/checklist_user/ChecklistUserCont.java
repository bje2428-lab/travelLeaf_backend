package dev.jpa.checklist_user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dev.jpa.checklist.Checklist;

@RestController
@RequestMapping("/checklist_user")
public class ChecklistUserCont {

    @Autowired
    private ChecklistUserService checklistUserService;

    /**
     * 1) 체크 등록
     * http://localhost:9100/checklist_user/add
     */
    @PostMapping("/add")
    public ChecklistUser addChecklist(@RequestBody ChecklistUserDTO dto) {
        return checklistUserService.save(dto);
    }

    /**
     * 2) 선택 내역 원본 조회 (CHECKLIST_USER 그대로)
     * http://localhost:9100/checklist_user/user/17/100
     */
    @GetMapping("/user/{userNo}/{batchId}")
    public List<ChecklistUser> getUserChecklist(
            @PathVariable("userNo") Long userNo,
            @PathVariable("batchId") Long batchId) {

        return checklistUserService.findByUserNoAndBatchId(userNo, batchId);
    }

    /**
     * 🔥 2-1) 디테일 화면용 마스터 조회
     * - DTO/Projection 없이 Checklist 엔티티 그대로 반환
     * - 프런트에서 itemName/description/category 바로 사용 가능
     *
     * http://localhost:9100/checklist_user/detail/17/100
     */
    @GetMapping("/detail/{userNo}/{batchId}")
    public List<Checklist> getUserChecklistDetail(
            @PathVariable("userNo") Long userNo,
            @PathVariable("batchId") Long batchId) {

        return checklistUserService.findDetailItemsAsMaster(userNo, batchId);
    }

    /**
     * 3) 선택 여부 확인
     */
    @GetMapping("/exists")
    public boolean exists(
            @RequestParam("userNo") Long userNo,
            @RequestParam("batchId") Long batchId,
            @RequestParam("itemId") Long itemId) {

        return checklistUserService.exists(userNo, batchId, itemId);
    }

    /**
     * 4) 체크 해제 (낱개 삭제)
     */
    @DeleteMapping("/delete")
    public void delete(
            @RequestParam("userNo") Long userNo,
            @RequestParam("batchId") Long batchId,
            @RequestParam("itemId") Long itemId) {

        checklistUserService.delete(userNo, batchId, itemId);
    }

    /**
     * 5) 전체 삭제
     */
    @DeleteMapping("/deleteAll/{userNo}/{batchId}")
    public void deleteAll(
            @PathVariable("userNo") Long userNo,
            @PathVariable("batchId") Long batchId) {

        checklistUserService.deleteAllByUserAndBatch(userNo, batchId);
    }

    /**
     * 6) 토글
     */
    @PostMapping("/toggle")
    public String toggle(
            @RequestParam("userNo") Long userNo,
            @RequestParam("batchId") Long batchId,
            @RequestParam("itemId") Long itemId) {

        return checklistUserService.toggle(userNo, batchId, itemId);
    }

    /**
     * 7) 관리자 itemId 조회
     */
    @GetMapping("/item/{itemId}")
    public List<ChecklistUser> findByItem(@PathVariable("itemId") Long itemId) {
        return checklistUserService.findByItemId(itemId);
    }

    /**
     * 8) 관리자 전체 조회
     */
    @GetMapping("/all")
    public List<ChecklistUser> findAll() {
        return checklistUserService.findAll();
    }
}
