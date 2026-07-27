package dev.jpa.bike_route;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class BikeRouteDTO {

    /* =========================
       목록 응답
    ========================= */
    @Getter
    @AllArgsConstructor
    public static class ListResponse {
        private Long routeId;
        private String routeName;
        private String region;
        private String city;
        private Double distanceKm;
        private Integer estimatedTimeMin;
        private Boolean hasPath;
    }

    /* =========================
       상세 응답
    ========================= */
    @Getter
    @AllArgsConstructor
    public static class DetailResponse {
        private Long routeId;
        private String routeName;
        private String region;
        private String city;

        private Double distanceKm;
        private Integer estimatedTimeMin;

        private String description;
        private List<String> highlights;
        private List<String> food;
        private List<String> tips;

        private Boolean hasPath;
    }

    /* =========================
       경로 좌표
    ========================= */
    @Getter
    @AllArgsConstructor
    public static class PathResponse {
        private Integer seq;
        private Double lat;
        private Double lng;
    }
}
