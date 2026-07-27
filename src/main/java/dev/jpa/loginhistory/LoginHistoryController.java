package dev.jpa.loginhistory;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/login-history")
public class LoginHistoryController {

    private final LoginHistoryService service;

    public LoginHistoryController(LoginHistoryService service) {
        this.service = service;
    }

    // 유저별 로그인 기록 조회 (페이징)
    @GetMapping("/{userno}")
    public ResponseEntity<?> getUserHistories(
            @PathVariable("userno") Long userno,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<LoginHistoryDto> histories = service.getLoginHistoryByUser(userno, page, size);

        return ResponseEntity.ok(Map.of(
                "content", histories.getContent(),
                "totalPages", histories.getTotalPages(),
                "number", histories.getNumber()
        ));
    }

    // 전체/검색 페이징
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<LoginHistoryDto> result = service.searchLoginHistory(keyword, page, size);

        return ResponseEntity.ok(Map.of(
                "content", result.getContent(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber()
        ));
    }

    // 특정 날짜 이전 삭제
    @DeleteMapping("/delete-before")
    public ResponseEntity<?> deleteBefore(@RequestParam("date") String date) {
        LocalDateTime dateTime = LocalDate.parse(date).atStartOfDay();
        service.deleteLoginHistoryBefore(dateTime);
        return ResponseEntity.ok(Map.of("message", date + " 이전 로그인 기록 삭제 완료"));
    }

    // 특정 기록 삭제
    @DeleteMapping("/{loginHistoryNo}")
    public ResponseEntity<?> deleteOne(@PathVariable Long loginHistoryNo) {
        service.deleteById(loginHistoryNo);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }
    
    @GetMapping("/summary")
    public ResponseEntity<?> getLoginSummary(HttpSession session) {
        Long userno = (Long) session.getAttribute("userno");
        if (userno == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "로그인 정보가 없습니다."));
        }

        String summary = service.generateLoginSummary(userno);

        return ResponseEntity.ok(Map.of(
                "summary", summary
        ));
    }
   

}
