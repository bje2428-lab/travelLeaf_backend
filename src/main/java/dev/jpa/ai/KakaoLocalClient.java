package dev.jpa.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class KakaoLocalClient {

    private final RestClient kakao;
    private final ObjectMapper om;

    @Value("${kakao.rest-api-key:}")
    private String kakaoRestApiKey;

    @Value("${kakao.base-url:https://dapi.kakao.com}")
    private String kakaoBaseUrl;

    public KakaoLocalClient(RestClient.Builder builder, ObjectMapper om) {
        this.om = om;
        // baseUrl은 property로도 바뀔 수 있어서 build 시점에 고정하지 않고,
        // 요청마다 uri에 상대경로만 쓰도록 baseUrl 적용
        this.kakao = builder
                .baseUrl("https://dapi.kakao.com")
                .build();
    }

    @Getter
    public static class PlaceDoc {
        private final String name;
        private final String categoryName;
        private final String address;
        private final String roadAddress;
        private final String placeUrl;
        private final double lat;
        private final double lng;
        private final Integer distanceM; // Kakao가 string으로 주는 distance를 int로 변환

        public PlaceDoc(String name, String categoryName, String address, String roadAddress,
                        String placeUrl, double lat, double lng, Integer distanceM) {
            this.name = name;
            this.categoryName = categoryName;
            this.address = address;
            this.roadAddress = roadAddress;
            this.placeUrl = placeUrl;
            this.lat = lat;
            this.lng = lng;
            this.distanceM = distanceM;
        }

        public String normKey() {
            String n = (name == null ? "" : name).trim().toLowerCase(Locale.ROOT);
            String u = (placeUrl == null ? "" : placeUrl).trim();
            return u.isEmpty() ? n + "@" + lat + "," + lng : u;
        }
    }

    /** 카테고리 주변검색 (CE7/FD6/AT4 등) */
    public List<PlaceDoc> searchByCategory(String categoryGroupCode, double lat, double lng, int radius, int size) {
        ensureKey();

        JsonNode res = kakao.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/category.json")
                        .queryParam("category_group_code", categoryGroupCode)
                        .queryParam("x", lng)  // Kakao: x=lng
                        .queryParam("y", lat)  // Kakao: y=lat
                        .queryParam("radius", radius)
                        .queryParam("size", size)
                        .queryParam("sort", "distance")
                        .build())
                .header("Authorization", "KakaoAK " + kakaoRestApiKey)
                .retrieve()
                .body(JsonNode.class);

        return parseDocs(res);
    }

    /** 키워드 검색 (선택적으로 쓰고 싶을 때) */
    public List<PlaceDoc> searchByKeyword(String query, double lat, double lng, int radius, int size) {
        ensureKey();

        JsonNode res = kakao.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("x", lng)
                        .queryParam("y", lat)
                        .queryParam("radius", radius)
                        .queryParam("size", size)
                        .queryParam("sort", "distance")
                        .build())
                .header("Authorization", "KakaoAK " + kakaoRestApiKey)
                .retrieve()
                .body(JsonNode.class);

        return parseDocs(res);
    }

    private void ensureKey() {
        if (kakaoRestApiKey == null || kakaoRestApiKey.trim().isEmpty()) {
            throw new IllegalStateException("kakao.rest-api-key is missing");
        }
    }

    private List<PlaceDoc> parseDocs(JsonNode res) {
        if (res == null) return List.of();
        JsonNode docs = res.get("documents");
        if (docs == null || !docs.isArray()) return List.of();

        List<PlaceDoc> out = new ArrayList<>();
        for (JsonNode d : docs) {
            String name = d.path("place_name").asText("");
            String cat = d.path("category_name").asText("");
            String addr = d.path("address_name").asText("");
            String road = d.path("road_address_name").asText("");
            String url = d.path("place_url").asText("");
            double x = safeDouble(d.path("x").asText(""));
            double y = safeDouble(d.path("y").asText(""));
            Integer dist = safeInt(d.path("distance").asText(""));

            if (name.isBlank()) continue;
            if (!Double.isFinite(y) || !Double.isFinite(x) || y == 0 || x == 0) continue;

            out.add(new PlaceDoc(name, cat, addr, road, url, y, x, dist));
        }
        return out;
    }

    private double safeDouble(String s) {
        try { return Double.parseDouble(String.valueOf(s)); } catch (Exception e) { return 0; }
    }

    private Integer safeInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
