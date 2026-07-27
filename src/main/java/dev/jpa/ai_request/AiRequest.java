package dev.jpa.ai_request;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_REQUEST")
@Getter
@Setter
@ToString
public class AiRequest {

    /** AI 요청 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_request_seq")
    @SequenceGenerator(
            name = "ai_request_seq",
            sequenceName = "AI_REQUEST_SEQ",
            allocationSize = 1
    )
    @Column(name = "REQUEST_ID")
    private Long requestId;

    /** 요청 사용자 */
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;

    /** AI 타입 (PLAN / WEATHER / PLACE) */
    @Column(name = "AI_TYPE", nullable = false)
    private String aiType;

    /** 입력 요약 (프롬프트 요약) */
    @Lob
    @Column(name = "INPUT_SUMMARY")
    private String inputSummary;

    /** 생성 시각 */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public AiRequest() {}

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
