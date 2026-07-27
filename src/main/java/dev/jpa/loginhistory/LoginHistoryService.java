package dev.jpa.loginhistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.user.User;
import dev.jpa.user.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginHistoryService {

    private final LoginHistoryRepository repository;
    private final UserService userService;

    // OpenAI API Key
    private final String apiKey = "openai_api_key";

    private final RestTemplate restTemplate = new RestTemplate();

    public LoginHistoryService(
        LoginHistoryRepository repository,
        UserService userService
    ) {
        this.repository = repository;
        this.userService = userService;
    }

    // ===============================
    // 로그인 기록 저장
    // ===============================
    public void saveLoginHistory(Long userno, String ip, String agent) {

        LocalDateTime now = LocalDateTime.now();

        boolean suspicious = isSuspiciousLogin(
                userno,
                now,
                ip,
                agent
        );

        LoginHistory history = new LoginHistory();
        history.setUserno(userno);
        history.setLoginAt(now);
        history.setIpAddress(ip);
        history.setUserAgent(agent);

        // 이상 로그인 메일 보내기
        if (suspicious) {
            User user = userService.findByUserno(userno);
            if (user != null && user.getEmail() != null) {
                userService.sendSuspiciousLoginMail(
                        user.getEmail(),
                        "해외 IP 로그인 감지"
                );
            }
        }

        repository.save(history);
    }

    // ===============================
    // 로그인 기록 조회 / 검색 / 삭제
    // ===============================
    public Page<LoginHistoryDto> getLoginHistoryByUser(Long userno, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoginHistory> histories =
                repository.findByUsernoOrderByLoginAtDesc(userno, pageable);

        List<LoginHistoryDto> dtoList = histories.stream()
                .map(lh -> new LoginHistoryDto(
                        lh.getLoginHistoryNo(),
                        lh.getUser() != null ? lh.getUser().getUserid() : "-",
                        lh.getUser() != null ? lh.getUser().getNickname() : "-",
                        lh.getUser() != null ? lh.getUser().getName() : "-",
                        lh.getLoginAt(),
                        lh.getIpAddress(),
                        lh.getUserAgent()
                ))
                .toList();

        return new PageImpl<>(dtoList, pageable, histories.getTotalElements());
    }

    public Page<LoginHistoryDto> searchLoginHistory(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && keyword.isBlank()) keyword = null;

        Page<LoginHistory> histories =
                repository.searchLoginHistory(keyword, pageable);

        List<LoginHistoryDto> dtoList = histories.stream()
                .map(lh -> new LoginHistoryDto(
                        lh.getLoginHistoryNo(),
                        lh.getUser() != null ? lh.getUser().getUserid() : "-",
                        lh.getUser() != null ? lh.getUser().getNickname() : "-",
                        lh.getUser() != null ? lh.getUser().getName() : "-",
                        lh.getLoginAt(),
                        lh.getIpAddress(),
                        lh.getUserAgent()
                ))
                .toList();

        return new PageImpl<>(dtoList, pageable, histories.getTotalElements());
    }

    public void deleteLoginHistoryBefore(LocalDateTime dateTime) {
        List<LoginHistory> toDelete = repository.findAll().stream()
                .filter(h -> h.getLoginAt().isBefore(dateTime))
                .toList();
        repository.deleteAll(toDelete);
    }

    public void deleteById(Long loginHistoryNo) {
        repository.deleteById(loginHistoryNo);
    }

    // ===============================
    // 이상 로그인 판단 (해외 IP)
    // ===============================
    public boolean isSuspiciousLogin(
        Long userNo,
        LocalDateTime now,
        String ipAddress,
        String userAgent
    ) {

        if (ipAddress == null) return false;

        boolean isForeignIp = !isKoreanIp(ipAddress);
        return isForeignIp;
    }

    private boolean isKoreanIp(String ip) {
        // 학원/국내 IP 범위만 한국 IP로 간주
        if (ip.startsWith("121.")) return true;

        // 그 외는 해외 IP
        return false;
    }

    // ===============================
    // AI 로그인 기록 요약
    // ===============================
    public String generateLoginSummary(Long userno) {

        // 최근 10개 로그인 기록 가져오기
        List<LoginHistory> histories = repository.findTop10ByUsernoOrderByLoginAtDesc(userno);

        if (histories.isEmpty()) return "최근 로그인 기록이 없습니다.";

        // 문자열로 변환
        StringBuilder sb = new StringBuilder();
        for (LoginHistory h : histories) {
            sb.append(String.format("로그인 시간: %s, IP: %s, 브라우저/디바이스: %s\n",
                    h.getLoginAt(), h.getIpAddress(), h.getUserAgent()));
        }

        // AI에게 요약 요청
        String prompt = "아래 로그인 기록을 분석해서 2문장 정도로 요약해줘:\n" + sb;

        return callOpenAiChat(prompt);
    }

    private String callOpenAiChat(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("temperature", 0.3);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(url, request, Map.class);
        List choices = (List) response.get("choices");
        if (choices == null || choices.isEmpty()) return "AI 요약 생성 실패";

        Map message = (Map) ((Map) choices.get(0)).get("message");
        return message.get("content").toString();
    }
}
