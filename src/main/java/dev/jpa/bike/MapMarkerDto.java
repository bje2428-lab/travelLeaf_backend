package dev.jpa.bike;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MapMarkerDto {

    private Long id;
    private String type;   // REPAIR / RENTAL
    private String name;
    private String phone; // 대여소 전화번호
    private String openTime;
    private String address;
    private Double lat;
    private Double lng;

    // 수리점 전용
    private Boolean onsiteService;

    // 대여소 전용
    private Integer bikeCount;
}
