package dev.jpa.ai_plan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPlanRunResponseDTO {
  
  private Long requestId;

    /** AI 결과(JSON 문자열) */
    private String resultJson;
}
