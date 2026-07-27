package dev.jpa.checklist_batch;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "CHECKLIST_BATCH")
public class ChecklistBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checklist_batch_seq")
    @SequenceGenerator(
            name = "checklist_batch_seq",
            sequenceName = "CHECKLIST_BATCH_SEQ",
            allocationSize = 1
    )
    @Column(name = "BATCH_ID")
    private Long batchId;

    /** FK: USER_TB(USER_NO) */
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;

    /** 여행 제목 */
    @Column(name = "TITLE", nullable = false)
    private String title;

    /** 여행 지역 경로 (JSON 문자열) */
    @Lob
    @Column(name = "ROUTE_REGIONS", nullable = false)
    private String routeRegions;

    /** 여행 도시 경로 (JSON 문자열) */
    @Lob
    @Column(name = "ROUTE_CITIES")
    private String routeCities;

    /** 유저 경유지 (JSON 문자열) */
    @Lob
    @Column(name = "ROUTE_WAYPOINTS")
    private String routeWaypoints;

    /** 출발 지점 */
    @Column(name = "START_POINT")
    private String startPoint;

    /** 도착 지점 */
    @Column(name = "END_POINT")
    private String endPoint;

    /**
     * 여행 시작 일시
     * 포맷: yyyy-MM-ddTHH:mm
     * 예: 2026-01-12T09:30
     */
    @Column(name = "START_DATETIME", nullable = false, length = 16)
    private String startDatetime;

    /**
     * 여행 종료 일시
     * 포맷: yyyy-MM-ddTHH:mm
     */
    @Column(name = "END_DATETIME", nullable = false, length = 16)
    private String endDatetime;

    /** 생성 시각 (DB에서 SYSDATE) */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private String createdAt;

    public ChecklistBatch() {}
}
