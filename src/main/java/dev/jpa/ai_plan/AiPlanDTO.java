package dev.jpa.ai_plan;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AiPlanDTO {

    /** 체크리스트/여행 묶음 ID */
    private Long batchId;

    /** 사용자 번호 */
    private Long userNo;

    /** AI 요청 ID */
    private Long requestId;

    /** AI가 생성한 결과(JSON) */
    private String resultJson;

    /**
     * DTO → Entity 변환
     */
    public AiPlan toEntity() {
        AiPlan entity = new AiPlan();
        entity.setBatchId(batchId);
        entity.setUserNo(userNo);
        entity.setRequestId(requestId);
        entity.setResultJson(resultJson);
        return entity;
    }
}
