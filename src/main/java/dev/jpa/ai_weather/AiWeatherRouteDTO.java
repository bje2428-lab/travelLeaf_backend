package dev.jpa.ai_weather;

import java.time.LocalDate;
import java.util.List;

/**
 * ✅ 동선 기반 날씨 분석 응답 DTO
 *
 * 목적:
 * - DAY_NUMBER 기준
 * - 하루 안에서 동선에 등장한 "시군구(CITY)" 단위로 요약
 * - 대표 지역 ❌
 * - 위험도는 해당 시군구에서의 최악값 보존
 * - ✅ 기상 수치는 해당 시군구의 대표 수치(집계값)
 * - ✅ 대기질은 "등급"만 사용 (수치 제거)
 */
public class AiWeatherRouteDTO {

    /* =========================
       최상위 응답
    ========================= */
    public static class Response {

        private Long scheduleId;
        private List<DayRouteWeather> days;

        public Response(Long scheduleId, List<DayRouteWeather> days) {
            this.scheduleId = scheduleId;
            this.days = days;
        }

        public Long getScheduleId() {
            return scheduleId;
        }

        public List<DayRouteWeather> getDays() {
            return days;
        }
    }

    /* =========================
       하루(DAY_NUMBER) 단위
    ========================= */
    public static class DayRouteWeather {

        /** 일정상 day 번호 */
        private Integer dayNumber;

        /** 날짜 */
        private LocalDate date;

        /** 해당 day에 등장한 시군구별 요약 */
        private List<CityRouteWeather> cities;

        public DayRouteWeather(
                Integer dayNumber,
                LocalDate date,
                List<CityRouteWeather> cities
        ) {
            this.dayNumber = dayNumber;
            this.date = date;
            this.cities = cities;
        }

        public Integer getDayNumber() {
            return dayNumber;
        }

        public LocalDate getDate() {
            return date;
        }

        public List<CityRouteWeather> getCities() {
            return cities;
        }
    }

    /* =========================
       시군구(CITY) 단위 요약
    ========================= */
    public static class CityRouteWeather {

        /** CITY PK */
        private Long cityId;

        /** 행정 기준 지역명 (예: "경기도 성남시") */
        private String regionName;

        /** 동선상 등장 순서들 (ORDER_IN_DAY 목록) */
        private List<Integer> orders;

        /* ---------- 위험도 ---------- */

        /** 해당 시군구에서의 최종 위험도 (최악값) */
        private String riskLevel;

        private String riskLabel;
        private String riskColor;
        private String riskIcon;

        /** 위험 사유 목록 (중복 제거) */
        private List<String> riskReasons;

        /* ---------- 🌦️ 기상 수치 ---------- */

        private Double windSpeed;        // m/s
        private Double precipitation;    // mm
        private Double precipProb;       // %
        private Double tempMax;          // ℃
        private Double tempMin;          // ℃

        /* ---------- 🌫️ 대기질 ---------- */

        /** 대기질 예보 등급 (좋음/보통/나쁨/매우나쁨) */
        private String airGrade;

        public CityRouteWeather(
                Long cityId,
                String regionName,
                List<Integer> orders,

                String riskLevel,
                String riskLabel,
                String riskColor,
                String riskIcon,
                List<String> riskReasons,

                Double windSpeed,
                Double precipitation,
                Double precipProb,
                Double tempMax,
                Double tempMin,

                String airGrade
        ) {
            this.cityId = cityId;
            this.regionName = regionName;
            this.orders = orders;

            this.riskLevel = riskLevel;
            this.riskLabel = riskLabel;
            this.riskColor = riskColor;
            this.riskIcon = riskIcon;
            this.riskReasons = riskReasons;

            this.windSpeed = windSpeed;
            this.precipitation = precipitation;
            this.precipProb = precipProb;
            this.tempMax = tempMax;
            this.tempMin = tempMin;

            this.airGrade = airGrade;
        }

        public Long getCityId() {
            return cityId;
        }

        public String getRegionName() {
            return regionName;
        }

        public List<Integer> getOrders() {
            return orders;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public String getRiskLabel() {
            return riskLabel;
        }

        public String getRiskColor() {
            return riskColor;
        }

        public String getRiskIcon() {
            return riskIcon;
        }

        public List<String> getRiskReasons() {
            return riskReasons;
        }

        public Double getWindSpeed() {
            return windSpeed;
        }

        public Double getPrecipitation() {
            return precipitation;
        }

        public Double getPrecipProb() {
            return precipProb;
        }

        public Double getTempMax() {
            return tempMax;
        }

        public Double getTempMin() {
            return tempMin;
        }

        public String getAirGrade() {
            return airGrade;
        }
    }
}
