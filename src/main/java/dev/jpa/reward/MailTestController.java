package dev.jpa.reward;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailTestController {

    private final LevelUpMailService levelUpMailService;

    @PostMapping("/test-levelup")
    public ResponseEntity<?> testLevelUpMail(@RequestBody TestMailRequest req) {
        try {
            levelUpMailService.sendLevelUpMail(req.getEmail(), req.getLevel());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace(); // ✅ 콘솔에 원인 전체 출력
            return ResponseEntity.status(500).body(e.getClass().getName() + " : " + e.getMessage());
        }
    }


    @Getter
    @Setter
    public static class TestMailRequest {
        private String email;
        private int level;
    }
}
