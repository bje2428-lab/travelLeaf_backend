package dev.jpa.ai_place;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiPlaceResultDTO {

    private String placeName;
    private String description;
    private double confidence;
    private String sourceApi;
}
