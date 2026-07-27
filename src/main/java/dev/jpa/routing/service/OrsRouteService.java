package dev.jpa.routing.service;

import dev.jpa.routing.dto.BikeDistanceReq;
import org.springframework.beans.factory.annotation.Qualifier;   // ✅ 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrsRouteService {

    private final RestClient ors;

    @Value("${ors.api-key:}")
    private String apiKey;

    // ✅ 여기만 핵심 수정: orsRestClient로 고정 주입
    public OrsRouteService(@Qualifier("orsRestClient") RestClient orsRestClient) {
        this.ors = orsRestClient;
    }

    public double getBikeDistanceMeters(BikeDistanceReq req) {
        // ✅ 키 없으면 여기서만 막고, 서버는 살아있게
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ORS_API_KEY가 설정되지 않았어요. (Run Configurations 환경변수 또는 application.properties 확인)");
        }

        if (req == null || req.points == null || req.points.size() < 2) {
            throw new IllegalArgumentException("points는 최소 2개(출발/도착) 필요");
        }
        if (req.points.size() > 50) {
            throw new IllegalArgumentException("points는 최대 50개까지 가능");
        }

        String profile = switch (req.style) {
            case ROAD -> "cycling-road";
            case MOUNTAIN -> "cycling-mountain";
            default -> "cycling-regular";
        };

        // ORS coordinates는 [lng, lat]
        List<List<Double>> coords = req.points.stream()
                .map(p -> List.of(p.lng, p.lat))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("coordinates", coords);
        body.put("preference", "recommended");
        body.put("instructions", false);

        Map res = ors.post()
                .uri("/v2/directions/{profile}/geojson", profile)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List features = (List) res.get("features");
        if (features == null || features.isEmpty()) {
            throw new IllegalStateException("ORS 응답에 features가 없음");
        }

        Map f0 = (Map) features.get(0);
        Map props = (Map) f0.get("properties");
        Map summary = (Map) props.get("summary");

        Object distObj = summary.get("distance");
        if (!(distObj instanceof Number)) {
            throw new IllegalStateException("ORS 응답에 distance가 없음");
        }
        return ((Number) distObj).doubleValue();
    }
}
