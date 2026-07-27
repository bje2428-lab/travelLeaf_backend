package dev.jpa.ai_plan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_PLAN")
@Getter
@Setter
@ToString
public class AiPlan {

    /** AI 결과 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_plan_seq")
    @SequenceGenerator(
            name = "ai_plan_seq",
            sequenceName = "AI_PLAN_SEQ",
            allocationSize = 1
    )
    @Column(name = "AI_PLAN_ID")
    private Long aiPlanId;

    /** AI 실행 요청 ID */
    @Column(name = "REQUEST_ID", nullable = false)
    private Long requestId;

    /** 체크리스트/여행 묶음 ID */
    @Column(name = "BATCH_ID", nullable = false)
    private Long batchId;

    /** 사용자 번호 */
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;

    /** AI가 생성한 결과(JSON) */
    @Lob
    @Column(name = "RESULT_JSON", nullable = false)
    private String resultJson;

    /** 생성 시각 */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public AiPlan() {}

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
