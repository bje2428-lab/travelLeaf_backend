package dev.jpa.checklist_batch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChecklistBatchDTO {

    /** 사용자 번호 */
    private Long userNo;

    /** 여행 제목 (유저 입력) */
    private String title;

    /**
     * 여행 지역 경로 (JSON 문자열)
     * 예: ["서울","경기","강원"]
     */
    private String routeRegions;

    /**
     * ✅ 여행 도시 경로 (JSON 문자열)
     * 예: ["서울","성남","가평"]
     */
    private String routeCities;

    /**
     * 가고 싶은 장소(경유지, POI)
     * 예: ["남이섬","소양강댐"]
     */
    private String routeWaypoints;

    /** 출발 지점 */
    private String startPoint;

    /** 도착 지점 */
    private String endPoint;

    /** 여행 시작 일시 (yyyy-MM-dd'T'HH:mm) */
    private String startDatetime;

    /** 여행 종료 일시 (yyyy-MM-dd'T'HH:mm) */
    private String endDatetime;
}
