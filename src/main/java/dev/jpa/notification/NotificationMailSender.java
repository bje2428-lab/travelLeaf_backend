// dev/jpa/notification/NotificationMailSender.java
package dev.jpa.notification;

import dev.jpa.schedule.share.ScheduleShareLog;
import dev.jpa.schedule.share.ScheduleShareLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class NotificationMailSender {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    // ✅ 스케줄 공유 로그 업데이트용
    private final ScheduleShareLogRepository shareLogRepository;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:}")
    private String fromEmail;

    public NotificationMailSender(
            NotificationRepository notificationRepository,
            JavaMailSender mailSender,
            ScheduleShareLogRepository shareLogRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.shareLogRepository = shareLogRepository;
    }

    /**
     * 5초마다 PENDING 이메일 알림 최대 30개 발송
     * - 성공: NOTIFICATION.SENT + sentAt
     * - 실패: NOTIFICATION.FAIL + failReason
     *
     * ✅ relatedType=SCHEDULE_SHARE, relatedId 존재하면
     *    SCHEDULE_SHARE_LOG도 같이 업데이트(SENT/FAIL + sentAt/errorMsg)
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void sendPendingEmails() {
        // ✅ mail 설정이 없으면 전송 시도하지 않음
        if (mailHost == null || mailHost.isBlank()) return;

        // ✅ Repository가 Page<Notification> 반환이므로 getContent() 필요
        List<Notification> pending =
                notificationRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, 30))
                        .getContent();

        for (Notification n : pending) {
            // EMAIL만 처리
            if (!"EMAIL".equalsIgnoreCase(n.getChannel())) continue;

            try {
                // (선택) 중복 발송 방지: 먼저 SENDING으로 바꾸고 저장
                n.setStatus("SENDING");
                notificationRepository.save(n);

                if (n.getToEmail() == null || n.getToEmail().isBlank()) {
                    throw new IllegalArgumentException("to_email is empty");
                }

                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(n.getToEmail());
                msg.setSubject(n.getTitle());
                msg.setText(n.getBody());

                if (fromEmail != null && !fromEmail.isBlank()) {
                    msg.setFrom(fromEmail.trim());
                }

                mailSender.send(msg);

                // ✅ 성공 처리
                n.setStatus("SENT");
                n.setSentAt(LocalDateTime.now());
                n.setFailReason(null);
                notificationRepository.save(n);

                // ✅ 공유로그도 성공 처리(연결돼있으면)
                updateShareLogFromNotification(n, true, null);

            } catch (Exception e) {
                // ✅ 실패 처리
                n.setStatus("FAIL");
                String reason = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
                if (reason.length() > 1000) reason = reason.substring(0, 1000);
                n.setFailReason(reason);
                notificationRepository.save(n);

                // ✅ 공유로그도 실패 처리(연결돼있으면)
                updateShareLogFromNotification(n, false, reason);
            }
        }
    }

    private void updateShareLogFromNotification(Notification n, boolean success, String reason) {
        // relatedType / relatedId 로 공유로그를 업데이트
        if (n.getRelatedId() == null) return;
        if (n.getRelatedType() == null) return;
        if (!"SCHEDULE_SHARE".equalsIgnoreCase(n.getRelatedType())) return;

        Optional<ScheduleShareLog> opt = shareLogRepository.findById(n.getRelatedId());
        if (opt.isEmpty()) return;

        ScheduleShareLog log = opt.get();
        if (success) {
            log.setStatus("SENT");
            log.setSentAt(n.getSentAt() != null ? n.getSentAt() : LocalDateTime.now());
            log.setErrorMsg(null);
        } else {
            log.setStatus("FAIL");
            log.setErrorMsg(reason);
            // 실패도 “시도 시간” 기록하고 싶으면 아래 주석 해제
            // log.setSentAt(LocalDateTime.now());
        }
        shareLogRepository.save(log);
    }
}
