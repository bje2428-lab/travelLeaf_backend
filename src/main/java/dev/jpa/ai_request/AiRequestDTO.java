package dev.jpa.ai_request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 요청 전달용 DTO
 * - Entity 생성 책임 ❌
 * - 단순 데이터 컨테이너 역할만 수행
 */
@Getter
@Setter
@ToString
public class AiRequestDTO {

    /** 요청 사용자 */
    private Long userNo;

    /** AI 타입 (WEATHER, PLAN 등) */
    private String aiType;

    /** 입력 요약 (로그용) */
    private String inputSummary;

}
