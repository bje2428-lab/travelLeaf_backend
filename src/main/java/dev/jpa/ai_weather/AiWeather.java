package dev.jpa.ai_weather;

import dev.jpa.ai_request.AiRequest;
import dev.jpa.schedule.Schedule;
import dev.jpa.schedule_detail.ScheduleDetail;
import dev.jpa.location.Region;
import dev.jpa.location.City;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_WEATHER")
public class AiWeather {

    /* =========================
       PK
    ========================= */

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_weather_seq")
    @SequenceGenerator(
            name = "ai_weather_seq",
            sequenceName = "AI_WEATHER_SEQ",
            allocationSize = 1
    )
    @Column(name = "AI_WEATHER_ID")
    private Long aiWeatherId;

    /* =========================
       FK / 연관관계
    ========================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private AiRequest aiRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SCHEDULE_ID", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DETAIL_ID")
    private ScheduleDetail scheduleDetail;

    /** 날씨 분석 기준 광역 지역 (REGION_ID) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_ID")
    private Region region;

    /** 날씨 분석 기준 시군구 (CITY_ID) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID")
    private City city;

    /* =========================
       날짜 / 위치
    ========================= */

    @Column(name = "TARGET_DATE", nullable = false)
    private LocalDate targetDate;

    @Column(name = "LAT")
    private Double lat;

    @Column(name = "LNG")
    private Double lng;

    /** 표시용 문자열 (행정 기준: 광역 + 시군구) */
    @Column(name = "REGION_NAME", length = 100)
    private String regionName;

    /** MOVE / STAY / START / END / DAY */
    @Column(name = "WEATHER_TYPE", length = 20)
    private String weatherType;

    /* =========================
       🌦️ 기상 수치 (유지)
    ========================= */

    /** 풍속 (m/s) */
    @Column(name = "WIND_SPEED")
    private Double windSpeed;

    /** 강수량 (mm) */
    @Column(name = "PRECIPITATION")
    private Double precipitation;

    /** 강수확률 (%) */
    @Column(name = "PRECIP_PROB")
    private Double precipProb;

    /** 최고기온 (°C) */
    @Column(name = "TEMP_MAX")
    private Double tempMax;

    /** 최저기온 (°C) */
    @Column(name = "TEMP_MIN")
    private Double tempMin;

    /* =========================
       🌫️ 대기질 (등급만 사용)
    ========================= */

    /** 대기질 등급 (매우좋음/좋음/보통/나쁨/매우나쁨) */
    @Column(name = "AIR_GRADE", length = 20)
    private String airGrade;

    /* =========================
       위험도 / AI 결과
    ========================= */

    @Column(name = "RISK_LEVEL", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "RISK_REASON", length = 300)
    private String riskReason;

    @Lob
    @Column(name = "AI_MESSAGE")
    private String aiMessage;

    /* =========================
       AUDIT
    ========================= */

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    protected AiWeather() {}

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /* =========================
       Getter / Setter
    ========================= */

    public Long getAiWeatherId() {
        return aiWeatherId;
    }

    public AiRequest getAiRequest() {
        return aiRequest;
    }

    public void setAiRequest(AiRequest aiRequest) {
        this.aiRequest = aiRequest;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public ScheduleDetail getScheduleDetail() {
        return scheduleDetail;
    }

    public void setScheduleDetail(ScheduleDetail scheduleDetail) {
        this.scheduleDetail = scheduleDetail;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(String weatherType) {
        this.weatherType = weatherType;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public Double getPrecipProb() {
        return precipProb;
    }

    public void setPrecipProb(Double precipProb) {
        this.precipProb = precipProb;
    }

    public Double getTempMax() {
        return tempMax;
    }

    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }

    public Double getTempMin() {
        return tempMin;
    }

    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    public String getAirGrade() {
        return airGrade;
    }

    public void setAirGrade(String airGrade) {
        this.airGrade = airGrade;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public String getAiMessage() {
        return aiMessage;
    }

    public void setAiMessage(String aiMessage) {
        this.aiMessage = aiMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
