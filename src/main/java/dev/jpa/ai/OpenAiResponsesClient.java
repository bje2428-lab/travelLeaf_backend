package dev.jpa.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiResponsesClient {

    private final RestClient openAi;
    private final ObjectMapper om;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    // ✅ 여기서 확실히 지정: OpenAI용 RestClient만 주입
    public OpenAiResponsesClient(
            @Qualifier("openAiRestClient") RestClient openAi,
            ObjectMapper om
    ) {
        this.openAi = openAi;
        this.om = om;
    }

    public JsonNode generateJsonWithSchema(List<Map<String, Object>> inputMessages,
                                           ObjectNode jsonSchema,
                                           String schemaName,
                                           int maxOutputTokens) {

        ObjectNode body = om.createObjectNode();
        body.put("model", model);
        body.set("input", om.valueToTree(inputMessages));

        ObjectNode format = om.createObjectNode();
        format.put("type", "json_schema");
        format.put("name", schemaName);
        format.put("strict", true);
        format.set("schema", jsonSchema);

        ObjectNode text = om.createObjectNode();
        text.set("format", format);

        body.set("text", text);
        body.put("temperature", 0.2);
        body.put("max_output_tokens", maxOutputTokens);
        body.put("store", false);

        JsonNode resp = openAi.post()
                .uri("/responses")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        String outText = extractOutputText(resp);
        try {
            return om.readTree(outText);
        } catch (Exception e) {
            throw new RuntimeException("LLM output JSON parse 실패: " + outText, e);
        }
    }

    private String extractOutputText(JsonNode resp) {
        if (resp == null) throw new RuntimeException("OpenAI 응답이 null");

        JsonNode output = resp.get("output");
        if (output == null || !output.isArray()) {
            throw new RuntimeException("OpenAI 응답 output 없음: " + resp);
        }

        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) continue;
            JsonNode content = item.path("content");
            if (!content.isArray()) continue;
            for (JsonNode c : content) {
                if ("output_text".equals(c.path("type").asText())) {
                    return c.path("text").asText();
                }
            }
        }
        throw new RuntimeException("OpenAI output_text를 찾지 못함: " + resp);
    }
}
