package dev.jpa.ai_plan;

import lombok.Getter;
import lombok.Setter;

/**
 * AI PLAN 실행 요청 DTO
 *
 * - 프런트엔드에서 AI 실행 시 전달
 * - 저장(create) 용 DTO와 분리 (의도적으로)
 */
@Getter
@Setter
public class AiPlanRunDTO {

    /** 사용자 번호 */
    private Long userNo;

    /** 체크리스트 / 여행 묶음 ID */
    private Long batchId;
}
