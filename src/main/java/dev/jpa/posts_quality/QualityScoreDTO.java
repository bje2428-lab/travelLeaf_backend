package dev.jpa.posts_quality;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter @Setter
public class QualityScoreDTO {

    private Double readability;
    private Double originality;
    private Double usefulness;

    @JsonProperty("aiScore")
    private Double aiScore;

    @JsonProperty("spamScore")
    private Double spamScore;

    @JsonProperty("qualityGrade")
    private String qualityGrade;
}
