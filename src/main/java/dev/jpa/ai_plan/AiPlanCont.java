package dev.jpa.ai_plan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai_plan")
public class AiPlanCont {

    @Autowired
    private AiPlanService aiPlanService;

    @Autowired
    private AiPlanAiService aiPlanAiService;

    /**
     * 0) AI 실행 (백엔드 주도)
     *
     * POST /ai_plan/run
     *
     * body:
     * {
     *   "userNo": 1,
     *   "batchId": 10
     * }
     *
     * 응답:
     * {
     *   "requestId": 123,
     *   "resultJson": "..."
     * }
     */
    @PostMapping("/run")
    public AiPlanRunResponseDTO run(@RequestBody AiPlanRunDTO dto) {

        /* =========================
           최소 유효성 검증 (수정)
        ========================= */
        if (dto == null) {
            throw new IllegalArgumentException("AiPlanRunDTO가 null 입니다.");
        }
        if (dto.getUserNo() == null) {
            throw new IllegalArgumentException("userNo가 없습니다.");
        }
        if (dto.getBatchId() == null) {
            throw new IllegalArgumentException("batchId가 없습니다.");
        }

        /* =========================
           AI 실행 (백엔드에서 prompt 생성)
        ========================= */
        return aiPlanAiService.runPlanAi(
                dto.getUserNo(),
                dto.getBatchId()
        );
    }

    /**
     * 1) AI 결과 저장
     */
    @PostMapping("/create")
    public AiPlan create(@RequestBody AiPlanDTO dto) {
        return aiPlanService.save(dto);
    }

    /**
     * 2) 특정 batchId 전체 조회
     */
    @GetMapping("/batch/{batchId}")
    public List<AiPlan> findByBatch(@PathVariable("batchId") Long batchId) {
        return aiPlanService.findByBatchId(batchId);
    }

    /**
     * 3) 특정 batchId 최신 1건
     */
    @GetMapping("/batch/{batchId}/latest")
    public AiPlan findLatestByBatch(@PathVariable("batchId") Long batchId) {
        return aiPlanService.findLatestByBatchId(batchId)
                .orElse(null);
    }

    /**
     * 4) 사용자 기준 조회
     */
    @GetMapping("/user/{userNo}")
    public List<AiPlan> findByUser(@PathVariable("userNo") Long userNo) {
        return aiPlanService.findByUserNo(userNo);
    }

    /**
     * 5) 단건 조회
     */
    @GetMapping("/{aiPlanId}")
    public AiPlan findOne(@PathVariable("aiPlanId") Long aiPlanId) {
        return aiPlanService.findById(aiPlanId).orElse(null);
    }

    /**
     * 6) 관리자 전체 조회
     */
    @GetMapping("/all")
    public List<AiPlan> findAll() {
        return aiPlanService.findAll();
    }
}
