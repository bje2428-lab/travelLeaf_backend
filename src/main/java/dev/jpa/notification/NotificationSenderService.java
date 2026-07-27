// dev/jpa/notification/NotificationSenderService.java
package dev.jpa.notification;

import dev.jpa.schedule.share.ScheduleShareLog;
import dev.jpa.schedule.share.ScheduleShareLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationSenderService {

    private final NotificationRepository notificationRepository;
    private final ScheduleShareLogRepository shareLogRepository;
    private final JavaMailSender mailSender;

    public NotificationSenderService(
            NotificationRepository notificationRepository,
            ScheduleShareLogRepository shareLogRepository,
            JavaMailSender mailSender
    ) {
        this.notificationRepository = notificationRepository;
        this.shareLogRepository = shareLogRepository;
        this.mailSender = mailSender;
    }

    /**
     * 10초마다 PENDING 알림 최대 20개 발송
     * - 성공: Notification SENT + sentAt
     * - 실패: Notification FAIL + failReason
     * - relatedType=SCHEDULE_SHARE, relatedId=share_log_id 인 경우:
     *     ShareLog도 같이 SENT/FAIL 및 sentAt/errorMsg 갱신
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void sendPendingEmails() {
        // ✅ Repository가 Page<Notification> 반환이므로 getContent() 필요
        List<Notification> pending =
                notificationRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, 20))
                        .getContent();

        for (Notification n : pending) {
            LocalDateTime now = LocalDateTime.now();

            try {
                if (!"EMAIL".equalsIgnoreCase(n.getChannel())) {
                    // 이메일 채널만 처리
                    continue;
                }

                if (n.getToEmail() == null || n.getToEmail().isBlank()) {
                    n.setStatus("FAIL");
                    n.setFailReason("to_email is empty");
                    syncShareLogFailIfAny(n, "to_email is empty");
                    notificationRepository.save(n);
                    continue;
                }

                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(n.getToEmail());
                msg.setSubject(n.getTitle());
                msg.setText(n.getBody());

                mailSender.send(msg);

                n.setStatus("SENT");
                n.setSentAt(now);
                n.setFailReason(null);
                notificationRepository.save(n);

                syncShareLogSentIfAny(n, now);

            } catch (Exception e) {
                String reason = (e.getMessage() == null) ? "mail send failed" : e.getMessage();

                n.setStatus("FAIL");
                n.setFailReason(trimTo1000(reason));
                notificationRepository.save(n);

                syncShareLogFailIfAny(n, trimTo1000(reason));
            }
        }
    }

    private void syncShareLogSentIfAny(Notification n, LocalDateTime now) {
        if (!"SCHEDULE_SHARE".equalsIgnoreCase(n.getRelatedType())) return;
        if (n.getRelatedId() == null) return;

        Optional<ScheduleShareLog> opt = shareLogRepository.findById(n.getRelatedId());
        if (opt.isEmpty()) return;

        ScheduleShareLog log = opt.get();
        log.setStatus("SENT");
        log.setSentAt(now);
        log.setErrorMsg(null);
        shareLogRepository.save(log);
    }

    private void syncShareLogFailIfAny(Notification n, String reason) {
        if (!"SCHEDULE_SHARE".equalsIgnoreCase(n.getRelatedType())) return;
        if (n.getRelatedId() == null) return;

        Optional<ScheduleShareLog> opt = shareLogRepository.findById(n.getRelatedId());
        if (opt.isEmpty()) return;

        ScheduleShareLog log = opt.get();
        log.setStatus("FAIL");
        log.setErrorMsg(trimTo1000(reason));
        shareLogRepository.save(log);
    }

    private String trimTo1000(String s) {
        if (s == null) return null;
        return (s.length() > 1000) ? s.substring(0, 1000) : s;
    }
}
