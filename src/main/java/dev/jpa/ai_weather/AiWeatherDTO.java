package dev.jpa.ai_weather;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dev.jpa.ai_weather_core.RiskLevel;

/**
 * AI 날씨 분석 DTO
 */
public class AiWeatherDTO {

    /**
     * 일정별 날씨 분석 응답 DTO
     */
    public static class Response {

        private Long scheduleId;
        private List<DailyWeather> days;

        public Response(Long scheduleId, List<DailyWeather> days) {
            this.scheduleId = scheduleId;
            this.days = days;
        }

        public Long getScheduleId() {
            return scheduleId;
        }

        public List<DailyWeather> getDays() {
            return days;
        }
    }

    /**
     * 하루 단위 날씨 분석 DTO
     */
    public static class DailyWeather {

        /** 날짜 */
        private LocalDate date;

        /** 위험도 코드 (VERY_SAFE / SAFE / CAUTION / WARNING / DANGER) */
        private String riskLevel;

        /** 위험도 한글 라벨 */
        private String riskLabel;

        /** 위험도 색상 (HEX) */
        private String riskColor;

        /** 위험도 아이콘 */
        private String riskIcon;

        /** 위험 판단 사유 */
        private String riskReason;

        /** 사용자 안내 문구 (AI 결과) */
        private String message;

        /** ✅ 언제 분석된 정보인지 (DB 생성 시각) */
        private LocalDateTime analyzedAt;

        /**
         * 생성자
         * - Service/Controller에서 RiskLevel enum 기준으로 생성
         * - analyzedAt 은 AiWeather.createdAt 그대로 전달
         */
        public DailyWeather(
                LocalDate date,
                RiskLevel level,
                String riskReason,
                String message,
                LocalDateTime analyzedAt
        ) {
            this.date = date;
            this.riskLevel = level.name();
            this.riskLabel = level.getLabel();
            this.riskColor = level.getColor();
            this.riskIcon = level.getIcon();
            this.riskReason = riskReason;
            this.message = message;
            this.analyzedAt = analyzedAt;
        }

        public LocalDate getDate() {
            return date;
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

        public String getRiskReason() {
            return riskReason;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getAnalyzedAt() {
            return analyzedAt;
        }
    }
}
