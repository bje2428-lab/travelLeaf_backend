package dev.jpa.posts_embeddings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmbeddingParser {

    private final ObjectMapper om = new ObjectMapper();

    public List<Double> parseVector(String raw) {
        try {
            if (raw == null) throw new RuntimeException("임베딩 값이 null 입니다.");

            String s = raw.trim();

            // ✅ 1) DB에 이미 배열만 저장된 케이스: [0.1, 0.2, ...]
            if (s.startsWith("[")) {
                JsonNode arr = om.readTree(s);
                if (!arr.isArray()) throw new RuntimeException("임베딩 배열 형식이 아닙니다.");

                List<Double> out = new ArrayList<>(arr.size());
                for (JsonNode n : arr) out.add(n.asDouble());
                return out;
            }

            // ✅ 2) OpenAI 응답 전체 JSON 저장된 케이스: { "data": [ { "embedding": [...] } ] }
            if (s.startsWith("{")) {
                JsonNode root = om.readTree(s);

                JsonNode data = root.get("data");
                if (data == null || !data.isArray() || data.size() == 0) {
                    throw new RuntimeException("임베딩 JSON 파싱 실패: data 배열 없음");
                }

                JsonNode emb = data.get(0).get("embedding");
                if (emb == null || !emb.isArray()) {
                    throw new RuntimeException("임베딩 JSON 파싱 실패: embedding 배열 없음");
                }

                List<Double> out = new ArrayList<>(emb.size());
                for (JsonNode n : emb) out.add(n.asDouble());
                return out;
            }

            // ✅ 3) 어떤 형식인지 모름
            throw new RuntimeException("임베딩 형식이 올바르지 않습니다. (시작 문자가 [ 또는 { 가 아님)");

        } catch (Exception e) {
            throw new RuntimeException("임베딩 JSON 파싱 실패", e);
        }
    }
}
