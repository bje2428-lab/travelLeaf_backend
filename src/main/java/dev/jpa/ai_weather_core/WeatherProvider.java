package dev.jpa.ai_weather_core;

import java.time.LocalDate;

public interface WeatherProvider {

    /**
     * 하루 단위 날씨 조회
     *
     * @param targetDate 분석 대상 날짜
     * @param lat 위도 (도시 기준)
     * @param lng 경도 (도시 기준)
     */
    WeatherResult getDailyWeather(
            LocalDate targetDate,
            Double lat,
            Double lng
    );
}
