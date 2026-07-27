package dev.jpa.reward;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LevelUpMailService {

    private final JavaMailSender mailSender;

    /**
     * ✅ 보내는 사람 주소(메일 서버 계정) - application.yml/properties에서 세팅 권장
     * 예) spring.mail.username=xxx@yyy.com
     */
    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendLevelUpMail(String toEmail, int newLevel) {
        if (toEmail == null || toEmail.trim().isBlank()) return;

        String to = toEmail.trim();

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // multipart=false, encoding="UTF-8"
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[자전거여행] 레벨업 축하드립니다! 🎉");

            // ✅ from 설정 (서버에 따라 필수)
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }

            String text =
                    "레벨업 축하드립니다~~ 🎉\n" +
                    "현재 레벨: " + newLevel + "\n\n" +
                    "감사합니다!";

            helper.setText(text, false); // false = plain text

            mailSender.send(message);

            System.out.println("[LEVEL-UP MAIL] sent to=" + to + " level=" + newLevel);

        } catch (MailException | MessagingException e) {
            // ✅ 실패 원인 로그 남기기
            System.out.println("[LEVEL-UP MAIL] send failed to=" + to + " level=" + newLevel
                    + " / " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new IllegalStateException("메일 전송 실패: " + e.getMessage(), e);
        }
    }
}
