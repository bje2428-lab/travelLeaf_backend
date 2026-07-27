package dev.jpa.ai.moderation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)   // 🔥 OpenAI가 필드 추가해도 안전
public class AiModerationResult {

    // OpenAI JSON: "toxicity_score"
    @JsonProperty("toxicity_score")
    private double toxicityScore;

    // OpenAI JSON: "reason"
    private String reason;
}
