package dev.jpa.ai_plan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoPlaceDTO {

    /** 카카오 place id (documents.id) */
    private String placeId;

    /** 장소명 */
    private String placeName;

    /** 도로명 주소 (road_address_name) */
    private String roadAddress;

    /** 지번 주소 (address_name) */
    private String address;

    /** 위도 (y) */
    private double lat;

    /** 경도 (x) */
    private double lng;
}
