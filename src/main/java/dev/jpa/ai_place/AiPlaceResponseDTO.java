package dev.jpa.ai_place;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiPlaceResponseDTO {
    private String placeName;
    private String description;
    private Double confidence;
}
