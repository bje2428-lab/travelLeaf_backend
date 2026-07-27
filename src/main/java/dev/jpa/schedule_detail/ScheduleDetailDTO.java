// dev.jpa.schedule_detail.ScheduleDetailDTO.java
package dev.jpa.schedule_detail;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduleDetailDTO {

    /* =========================
       CREATE REQUEST
    ========================= */
    @Getter
    @Setter
    public static class CreateRequest {
        private Long scheduleId;

        private Integer dayNumber;
        private Integer orderInDay;

        private Long placeId;        // Kakao place_id
        private String placeName;    // AI / 사용자 입력

        private String category;
        private String address;
        private Double lat;
        private Double lng;

        private String stopType;

        private LocalDateTime startTime;
        private LocalDateTime endTime;

        private Long cost;
        private String memo;
        private Double distanceKM;

        /* =========================
           ✅ AI 지역 정보 (문자열)
        ========================= */
        private String region;   // ex) "경기도"
        private String city;     // ex) "성남시"
    }

    /* =========================
       RESPONSE
    ========================= */
    @Getter
    @Setter
    public static class Response {

        private Long detailId;
        private Long scheduleId;

        private Integer dayNumber;
        private Integer orderInDay;

        private Long placeId;

        private String place_name;
        private String road_address_name;
        private Double lat;
        private Double lng;

        private String stopType;

        private String startTime;
        private String endTime;

        private Long cost;
        private String memo;
        private Double distanceKM;

        /* =========================
           ✅ 추가: 저장된 지역/도시 ID
           (날씨 / 분석용)
        ========================= */
        private Long regionId;
        private Long cityId;

        private String createdAt;
        private String updatedAt;
    }

    /* =========================
       FORMATTER
    ========================= */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String fmt(LocalDateTime t) {
        return t == null ? null : t.format(FMT);
    }

    /* =========================
       ENTITY → RESPONSE
    ========================= */
    public static Response fromEntity(ScheduleDetail detail) {
        Response res = new Response();

        res.setDetailId(detail.getDetailId());

        if (detail.getSchedule() != null) {
            res.setScheduleId(detail.getSchedule().getScheduleId());
        }

        res.setDayNumber(detail.getDayNumber());
        res.setOrderInDay(detail.getOrderInDay());
        res.setStopType(detail.getStopType());

        if (detail.getPlace() != null) {
            res.setPlaceId(detail.getPlace().getPlaceId());
            res.setPlace_name(detail.getPlace().getName());
            res.setRoad_address_name(detail.getPlace().getAddress());
            res.setLat(detail.getPlace().getLat());
            res.setLng(detail.getPlace().getLng());
        } else {
            res.setPlaceId(null);
            res.setPlace_name(detail.getPlaceName());
            res.setRoad_address_name(null);
            res.setLat(null);
            res.setLng(null);
        }

        res.setStartTime(fmt(detail.getStartTime()));
        res.setEndTime(fmt(detail.getEndTime()));

        res.setCost(detail.getCost());
        res.setMemo(detail.getMemo());
        res.setDistanceKM(detail.getDistanceKM());

        /* ✅ 핵심 추가 */
        res.setRegionId(detail.getRegionId());
        res.setCityId(detail.getCityId());

        res.setCreatedAt(fmt(detail.getCreatedAt()));
        res.setUpdatedAt(fmt(detail.getUpdatedAt()));

        return res;
    }
}
