package dev.jpa.websocket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class HotelAiChatService {

    /**
     * ✅ 여러 AI 서버 URL (콤마 구분)
     * - 설정이 없어도 Spring이 죽지 않도록 기본값을 둔다.
     *
     * application.properties 예시:
     * ai.server.urls=http://127.0.0.1:8000,http://121.160.42.71:8000
     */
    @Value("${ai.server.urls:http://127.0.0.1:8000}")
    private String aiServerUrls;

    private static final String CHAT_PATH = "/chat/hotel";

    public String ask(String hotelExtId, String userMessage) {

        if (hotelExtId == null || hotelExtId.isBlank()) {
            return "호텔 정보가 확인되지 않아 답변을 드리기 어렵습니다.";
        }

        if (userMessage == null || userMessage.isBlank()) {
            return "메시지를 입력해 주세요 😊";
        }

        // ✅ 1) 서버 URL 목록 파싱
        List<String> servers = parseServerUrls(aiServerUrls);

        // ✅ 2) 유효한 서버가 하나도 없으면 fallback
        if (servers.isEmpty()) {
            servers.add("http://127.0.0.1:8000");
        }

        // ✅ 3) 지금은 첫 번째 서버만 사용 (최소 수정 유지)
        String baseUrl = servers.get(0);

        String url = normalize(baseUrl) + CHAT_PATH;

        HotelAiChatRequest req = new HotelAiChatRequest(hotelExtId, userMessage);

        try {
            RestTemplate restTemplate = new RestTemplate();

            HotelAiChatResponse res =
                restTemplate.postForObject(url, req, HotelAiChatResponse.class);

            if (res == null || res.getReply() == null || res.getReply().isBlank()) {
                return "답변 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.";
            }

            return res.getReply();

        } catch (RestClientException e) {
            return "현재 호텔 AI 응답이 지연되고 있습니다. 잠시 후 다시 문의해 주세요.";
        }
    }

    /**
     * ai.server.urls 문자열을 URL 리스트로 파싱
     * - 콤마로 split
     * - 공백 제거
     * - 빈 값 제거
     */
    private List<String> parseServerUrls(String raw) {
        List<String> list = new ArrayList<>();

        if (raw == null) return list;

        String[] urls = raw.split(",");
        for (String u : urls) {
            if (u == null) continue;
            String t = u.trim();
            if (t.isEmpty()) continue;
            list.add(t);
        }

        return list;
    }

    private String normalize(String baseUrl) {
        if (baseUrl == null) return "";
        return baseUrl.endsWith("/")
            ? baseUrl.substring(0, baseUrl.length() - 1)
            : baseUrl;
    }
}
