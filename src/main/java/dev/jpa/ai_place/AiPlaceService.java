package dev.jpa.ai_place;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Date;

@Service
public class AiPlaceService {

    private final AiPlaceResultRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String API_KEY = System.getenv("OPENAI_API_KEY");

    public AiPlaceService(AiPlaceResultRepository repo) {
        this.repo = repo;
    }

    public AiPlaceResponseDTO analyze(MultipartFile image) {

        String placeName = "대한민국 (분석 실패)";
        String description = "AI가 사진을 분석했으나 정확한 장소를 특정하지 못했습니다.";
        double confidence = 0.3;

        try {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());

            String body = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {
                  "role": "user",
                  "content": [
                    {
                      "type": "text",
                      "text": "이 사진은 대한민국에서 촬영되었다. 사진을 보고 실제 존재하는 가장 가능성 높은 장소명을 하나만 말해라."
                    },
                    {
                      "type": "image_url",
                      "image_url": {
                        "url": "data:image/jpeg;base64,%s"
                      }
                    }
                  ]
                }
              ]
            }
            """.formatted(base64);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());
            placeName = root.at("/choices/0/message/content").asText();
            confidence = 0.9;

        } catch (Exception e) {
            e.printStackTrace(); // 서버는 죽이지 않음
        }

        AiPlaceResult result = new AiPlaceResult();
        result.setPlaceName(placeName);
        result.setDescription(description);
        result.setConfidence(confidence);
        result.setSourceApi("OPENAI");
        result.setCreatedAt(new Date());

        repo.save(result);

        return new AiPlaceResponseDTO(placeName, description, confidence);
    }
}
