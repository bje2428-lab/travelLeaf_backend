package dev.jpa.schedule_detail;

import dev.jpa.schedule.Schedule;
import dev.jpa.places.Places;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_DETAIL")
public class ScheduleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_detail_seq")
    @SequenceGenerator(
            name = "schedule_detail_seq",
            sequenceName = "SCHEDULE_DETAIL_SEQ",
            allocationSize = 1
    )
    @Column(name = "DETAIL_ID")
    private Long detailId;

    /* =========================
       연관관계
    ========================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SCHEDULE_ID", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLACE_ID")
    private Places place;

    /* =========================
       ✅ 추가: 지역/도시 FK (날씨/분석용)
       - 현재는 REGION/CITY 엔티티 정보가 없으므로
         연관관계(@ManyToOne)로 임의 매핑하지 않고,
         컬럼(Long)으로만 안전하게 저장
    ========================= */

    @Column(name = "REGION_ID")
    private Long regionId;

    @Column(name = "CITY_ID")
    private Long cityId;

    /* =========================
       일정 정보
    ========================= */

    @Column(name = "DAY_NUMBER", nullable = false)
    private Integer dayNumber;

    @Column(name = "ORDER_IN_DAY", nullable = false)
    private Integer orderInDay;

    @Column(name = "STOP_TYPE", nullable = false, length = 10)
    private String stopType; // START / END / WAYPOINT / COURSE / STAY

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "COST")
    private Long cost;

    @Column(name = "MEMO", length = 1000)
    private String memo;

    @Column(name = "DISTANCE_KM")
    private Double distanceKM;

    /* =========================
       🔥 장소명 (AI / 사용자 입력용)
       - PLACE_ID 없을 때 사용
       - AI 일정 핵심 컬럼
    ========================= */

    @Column(name = "PLACE_NAME", length = 300)
    private String placeName;

    /* =========================
       AUDIT
    ========================= */

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected ScheduleDetail() {}

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* =========================
       Getter / Setter
    ========================= */

    public Long getDetailId() { return detailId; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public Places getPlace() { return place; }
    public void setPlace(Places place) { this.place = place; }

    /* ===== region/city FK ===== */

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }

    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }

    public Integer getOrderInDay() { return orderInDay; }
    public void setOrderInDay(Integer orderInDay) { this.orderInDay = orderInDay; }

    public String getStopType() { return stopType; }
    public void setStopType(String stopType) { this.stopType = stopType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getCost() { return cost; }
    public void setCost(Long cost) { this.cost = cost; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public Double getDistanceKM() { return distanceKM; }
    public void setDistanceKM(Double distanceKM) { this.distanceKM = distanceKM; }

    /* ===== placeName ===== */

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    /* ===== audit ===== */

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
