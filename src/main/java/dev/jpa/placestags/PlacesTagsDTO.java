package dev.jpa.placestags;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PlacesTagsDTO {

    @Getter @Setter
    public static class SaveRequest {
        private Long placeId;
        private List<String> tags; // ["#한강", "야경", "#라이더맛집"]
    }

    @Getter @Setter
    public static class ListResponse {
        private Long placeId;
        private List<String> tags; // ["#한강", "#야경", "#라이더맛집"]
    }
}
