package dev.jpa.ai_weather_core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class KmaWeatherProvider implements WeatherProvider {

    private static final String SERVICE_KEY =
            "Trjidr2ZdCsY7VBk6inh8Z0w0fggBa%2BeWEQhugAVRj0CzSr0CQU%2F5XHS%2BPLdeK3s6jWG2j29jHMnRclOSKpHqA%3D%3D";

    private static final String BASE_URL =
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WeatherResult getDailyWeather(
            LocalDate targetDate,
            Double lat,
            Double lng
    ) {
        try {
            // 1) 위경도 → 격자
            GridConverter.GridPoint grid = GridConverter.toGrid(lat, lng);

            // 2) 가장 최근 base_date / base_time 계산
            BaseDateTime base = resolveLatestBaseDateTime(targetDate);

            // 3) URL 구성
            String url = BASE_URL +
                    "?serviceKey=" + SERVICE_KEY +
                    "&pageNo=1" +
                    "&numOfRows=1000" +
                    "&dataType=JSON" +
                    "&base_date=" + base.baseDate +
                    "&base_time=" + base.baseTime +
                    "&nx=" + grid.nx() +
                    "&ny=" + grid.ny();

            String response = restTemplate.getForObject(new URI(url), String.class);

            JsonNode items = objectMapper.readTree(response)
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            Double minTemp = null;
            Double maxTemp = null;
            Double windSpeed = null;
            Double precipitation = null;
            Double rainProb = null;
            String precipType = null;

            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String value = item.path("fcstValue").asText();

                switch (category) {
                    case "TMN" -> minTemp = parseDouble(value);
                    case "TMX" -> maxTemp = parseDouble(value);
                    case "WSD" -> windSpeed = parseDouble(value);
                    case "PCP" -> precipitation = parsePcpMm(value);
                    case "POP" -> rainProb = parseDouble(value);
                    case "PTY" -> precipType = value;
                }
            }

            return new WeatherResult(
                    minTemp,
                    maxTemp,
                    windSpeed,
                    precipitation,
                    rainProb,
                    precipType,
                    false
            );

        } catch (Exception e) {
            throw new RuntimeException("기상청 날씨 조회 실패", e);
        }
    }

    /* =========================
       ✅ 가장 최근 발표 base_date / base_time 계산
    ========================= */
    private BaseDateTime resolveLatestBaseDateTime(LocalDate targetDate) {

        // 기상청 발표 시각
        int[] TIMES = {23, 20, 17, 14, 11, 8, 5, 2};

        LocalDateTime now = LocalDateTime.now();
        LocalDate baseDate = targetDate.isAfter(now.toLocalDate())
                ? now.toLocalDate()
                : targetDate;

        int hour = now.getHour();

        for (int t : TIMES) {
            if (hour >= t) {
                return new BaseDateTime(
                        baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                        String.format("%02d00", t)
                );
            }
        }

        // 새벽 0~1시는 전날 23시
        return new BaseDateTime(
                baseDate.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                "2300"
        );
    }

    private record BaseDateTime(String baseDate, String baseTime) {}

    private Double parseDouble(String v) {
        try {
            return Double.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parsePcpMm(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty() || "강수없음".equals(v)) return 0.0;
        if (v.contains("미만")) return 1.0;
        v = v.replace("mm", "").trim();
        try {
            return Double.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }
}
