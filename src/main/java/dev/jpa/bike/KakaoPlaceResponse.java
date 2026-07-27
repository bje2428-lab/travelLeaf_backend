package dev.jpa.bike;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 여기에 추가
public class KakaoPlaceResponse {
    private List<Document> documents;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 내부 Document에도 추가
    public static class Document {
        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("road_address_name")
        private String roadAddressName;

        private String x; // lng
        private String y; // lat
        private String phone;

        @JsonProperty("category_name")
        private String categoryName;

        private String distance;
    }
}
