package dev.jpa.ai_plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class KakaoPlaceSearchService {

    @Value("${kakao.rest.api-key}")
    private String kakaoRestKey;

    private static final String KAKAO_PLACE_URL =
            "https://dapi.kakao.com/v2/local/search/keyword.json";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<KakaoPlaceDTO> search(String keyword, int size) {

        try {
            String query = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            String url = KAKAO_PLACE_URL +
                    "?query=" + query +
                    "&size=" + size;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoRestKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode docs = root.path("documents");

            List<KakaoPlaceDTO> results = new ArrayList<>();

            for (JsonNode d : docs) {
                KakaoPlaceDTO dto = new KakaoPlaceDTO();
                dto.setPlaceName(d.path("place_name").asText());
                dto.setAddress(
                        d.path("road_address_name").asText().isEmpty()
                                ? d.path("address_name").asText()
                                : d.path("road_address_name").asText()
                );
                dto.setLat(d.path("y").asDouble());
                dto.setLng(d.path("x").asDouble());

                results.add(dto);
            }

            return results;

        } catch (Exception e) {
            throw new RuntimeException("카카오 장소 검색 실패", e);
        }
    }
}
