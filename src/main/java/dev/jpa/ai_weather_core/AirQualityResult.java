package dev.jpa.ai_weather_core;

import java.time.LocalDate;

/**
 * 미세먼지 / 대기질 예보 결과 DTO
 *
 * 정책:
 * - 대기질은 "예보 등급"만 사용
 * - 수치(PM10 / PM2.5)는 취급하지 않음
 */
public class AirQualityResult {

    /** 예보 날짜 */
    private final LocalDate date;

    /** 대기질 예보 등급 (좋음 / 보통 / 나쁨 / 매우나쁨) */
    private final String grade;

    public AirQualityResult(
            LocalDate date,
            String grade
    ) {
        this.date = date;
        this.grade = grade;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getGrade() {
        return grade;
    }
}
