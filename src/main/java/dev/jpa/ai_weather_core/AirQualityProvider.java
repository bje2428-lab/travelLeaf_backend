package dev.jpa.ai_weather_core;

import dev.jpa.location.Region;

import java.time.LocalDate;

/**
 * 미세먼지 / 대기질 예보 Provider
 *
 * 정책:
 * - 시군구(City) 단위 ❌
 * - 광역(Region) 단위 예보 등급만 사용
 * - 여행 날짜(targetDate) 기준 예보
 */
public interface AirQualityProvider {

    /**
     * 광역(Region) 기준 대기질 예보 조회
     *
     * @param region     광역 지역 (서울, 경기, 부산 등)
     * @param targetDate 여행 날짜
     */
    AirQualityResult getAirQuality(
            Region region,
            LocalDate targetDate
    );
}
