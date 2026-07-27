package dev.jpa.places;

import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

public class PlacesDTO {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long placeId;       // ✅ kakaoId를 PK로 쓸 거면 필수
        private String name;
        private String category;
        private Long regionId;
        private Long cityId;
        private String address;
        private Double lat;
        private Double lng;
        private Integer rating;
        private String sourceType;  // "KAKAO"
        private String sourceId;    // 문자열 kakaoId(선택)
        private String difficulty;
        private Double distanceKM;
    }

    @Getter
    @Setter
    public static class Response {
        private Long placeId;
        private String name;
        private String category;
        private Long regionId;
        private Long cityId;
        private String address;
        private Double lat;
        private Double lng;
        private Integer rating;
        private String sourceType;
        private String sourceId;
        private String isActive;
        private String createdAt;
        private String updatedAt;
        private String difficulty;
        private Double distanceKM;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Response fromEntity(Places place) {
        Response res = new Response();
        res.setPlaceId(place.getPlaceId());
        res.setName(place.getName());
        res.setCategory(place.getCategory());
        res.setAddress(place.getAddress());
        res.setLat(place.getLat());
        res.setLng(place.getLng());
        res.setRating(place.getRating());
        res.setSourceType(place.getSourceType());
        res.setSourceId(place.getSourceId());
        res.setIsActive(place.getIsActive());
        res.setDifficulty(place.getDifficulty());
        res.setDistanceKM(place.getDistanceKM());

        if (place.getRegion() != null) res.setRegionId(place.getRegion().getRegionId());
        if (place.getCity() != null) res.setCityId(place.getCity().getCityId());

        res.setCreatedAt(place.getCreatedAt() != null ? place.getCreatedAt().format(FMT) : null);
        res.setUpdatedAt(place.getUpdatedAt() != null ? place.getUpdatedAt().format(FMT) : null);

        return res;
    }
}
