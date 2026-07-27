package dev.jpa.ai_weather_core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpa.location.Region;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class AirKoreaProvider implements AirQualityProvider {

    /**
     * ✅ 반드시 "인코딩 키"
     * ❌ 디코딩 키 절대 사용 금지
     */
    private static final String SERVICE_KEY =
        "Trjidr2ZdCsY7VBk6inh8Z0w0fggBa%2BeWEQhugAVRj0CzSr0CQU%2F5XHS%2BPLdeK3s6jWG2j29jHMnRclOSKpHqA%3D%3D";
        
    private static final String FORECAST_URL =
        "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth";

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AirQualityResult getAirQuality(Region region, LocalDate targetDate) {

        if (region == null || targetDate == null) {
            return new AirQualityResult(targetDate, null);
        }

        String regionName = region.getRegionName();
        if (regionName == null || regionName.isBlank()) {
            return new AirQualityResult(targetDate, null);
        }

        for (int i = 0; i <= 2; i++) {
            LocalDate searchDate = targetDate.minusDays(i);
            String grade = fetchGrade(regionName, searchDate, targetDate);
            if (grade != null) {
                return new AirQualityResult(targetDate, grade);
            }
        }

        return new AirQualityResult(targetDate, null);
    }

    private String fetchGrade(String regionName, LocalDate searchDate, LocalDate targetDate) {
      
      System.out.println("========== AirKorea CALL ==========");
      System.out.println("regionName  = " + regionName);
      System.out.println("searchDate  = " + searchDate);
      System.out.println("targetDate  = " + targetDate);
      System.out.println("==================================");

        try {
            URI uri = UriComponentsBuilder
                .fromHttpUrl(FORECAST_URL)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("returnType", "JSON")
                .queryParam("numOfRows", 100)
                .queryParam("pageNo", 1)
                .queryParam("searchDate", searchDate.format(DATE_FMT))
                .queryParam("informCode", "PM10")
                .build(true) // ✅ 핵심
                .toUri();

            System.out.println("[AirKorea URL] " + uri);

            String response = restTemplate.getForObject(uri, String.class);
            
            System.out.println("===== AirKorea RAW RESPONSE =====");
            System.out.println(response);
            System.out.println("=================================");

            JsonNode items = objectMapper.readTree(response)
                .path("response")
                .path("body")
                .path("items");
            
            System.out.println("items.isArray = " + items.isArray());
            System.out.println("items.size    = " + items.size());

            if (!items.isArray()) return null;

            LocalDate latestDate = null;
            List<String> grades = new ArrayList<>();

            for (JsonNode item : items) {
              
              System.out.println("---- ITEM ----");
              System.out.println("informData  = " + item.path("informData").asText());
              System.out.println("informGrade= " + item.path("informGrade").asText());

                LocalDate forecastDate =
                    LocalDate.parse(item.path("informData").asText(), DATE_FMT);

                if (forecastDate.isAfter(targetDate)) continue;
                if (latestDate != null && forecastDate.isBefore(latestDate)) continue;

                for (String part : item.path("informGrade").asText("").split(",")) {
                    String[] kv = part.split(":");
                    if (kv.length < 2) continue;

                    String area = kv[0].replaceAll("\\s+", "");
                    String grade = kv[1].trim();

                    if (matchesRegion(regionName, area)) {
                      System.out.println("MATCHED ▶ " + area + " = " + grade);
                      grades.add(grade);
                  }

                }

                if (!grades.isEmpty()) {
                    latestDate = forecastDate;
                }
            }

            return selectWorstGrade(grades);

        } catch (Exception e) {
            System.out.println("[AirKorea ERROR] " + e.getMessage());
            return null;
        }
    }

    private boolean matchesRegion(String regionName, String area) {

        String rn = regionName.replaceAll("\\s+", "");

        if (rn.contains("강원")) return area.startsWith("영동") || area.startsWith("영서");
        if (rn.contains("경기")) return area.startsWith("경기북부") || area.startsWith("경기남부");

        if (rn.contains("충청북")) return area.startsWith("충북");
        if (rn.contains("충청남")) return area.startsWith("충남");

        if (rn.contains("전라북")) return area.startsWith("전북");
        if (rn.contains("전라남")) return area.startsWith("전남");

        if (rn.contains("경상북")) return area.startsWith("경북");
        if (rn.contains("경상남")) return area.startsWith("경남");

        if (rn.contains("서울")) return area.startsWith("서울");
        if (rn.contains("부산")) return area.startsWith("부산");
        if (rn.contains("대구")) return area.startsWith("대구");
        if (rn.contains("인천")) return area.startsWith("인천");
        if (rn.contains("광주")) return area.startsWith("광주");
        if (rn.contains("대전")) return area.startsWith("대전");
        if (rn.contains("울산")) return area.startsWith("울산");
        if (rn.contains("세종")) return area.startsWith("세종");
        if (rn.contains("제주")) return area.startsWith("제주");

        return false;
    }

    private String selectWorstGrade(List<String> grades) {
        return grades.stream()
                .map(g -> g.replaceAll("\\s+", ""))
                .max((a, b) ->
                        RiskLevel.fromAirGrade(a)
                                .compareTo(RiskLevel.fromAirGrade(b)))
                .orElse(null);
    }
}
