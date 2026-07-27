package dev.jpa.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
//@CrossOrigin(origins = "*")
public class ImageProxyCont {

    private final RestClient restClient;
    private final String pexelsKey;

    public ImageProxyCont(
            @Value("${pexels.api-key:}") String pexelsKey
    ) {
        this.pexelsKey = pexelsKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.pexels.com")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @GetMapping("/pexels")
    public Map<String, Object> pexels(@RequestParam String query) {
        if (pexelsKey == null || pexelsKey.isBlank()) {
            return Map.of("success", false, "imageUrl", null, "reason", "NO_PEXELS_KEY");
        }

        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String path = "/v1/search?query=" + q + "&per_page=1&orientation=landscape";

        Map body = restClient.get()
                .uri(path)
                .header("Authorization", pexelsKey)
                .retrieve()
                .body(Map.class);

        // photos[0].src.landscape 꺼내기
        try {
            var photos = (java.util.List<Map>) body.get("photos");
            if (photos == null || photos.isEmpty()) return Map.of("success", false, "imageUrl", null);
            var src = (Map) photos.get(0).get("src");
            String imageUrl = (String) (src.get("landscape") != null ? src.get("landscape") : src.get("large"));
            return Map.of("success", imageUrl != null, "imageUrl", imageUrl);
        } catch (Exception e) {
            return Map.of("success", false, "imageUrl", null);
        }
    }
}
