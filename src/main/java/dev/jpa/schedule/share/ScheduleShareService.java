// dev/jpa/schedule/share/ScheduleShareService.java
package dev.jpa.schedule.share;

import dev.jpa.notification.Notification;
import dev.jpa.notification.NotificationRepository;
import dev.jpa.schedule.Schedule;
import dev.jpa.schedule.ScheduleRepository;
import dev.jpa.schedule.share.dto.ShareRequest;
import dev.jpa.schedule.share.dto.ShareResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class ScheduleShareService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleShareLogRepository shareLogRepository;
    private final NotificationRepository notificationRepository;

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RND = new SecureRandom();

    public ScheduleShareService(
            ScheduleRepository scheduleRepository,
            ScheduleShareLogRepository shareLogRepository,
            NotificationRepository notificationRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.shareLogRepository = shareLogRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * ✅ 공유 생성/갱신
     * - 기존 shareCode 있으면 재사용, 없으면 생성
     * - shareEnabled = 'Y'
     * - shareScope 는 DB CHECK에 맞춰 'LINK'/'PUBLIC'/'PRIVATE' 중 하나로 저장
     *
     * ✅ 시간 정책
     * - LINK 공유: 즉시 SENT + sentAt=now
     * - EMAIL 공유: 메일은 비동기 발송이므로 여기서는 PENDING 유지
     *              실제 발송 성공/실패는 NotificationSenderService가 Notification + ShareLog를 갱신
     */
    @Transactional
    public ShareResponse shareSchedule(Long scheduleId, ShareRequest req) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found: " + scheduleId));

        String channel = (req != null && req.getChannel() != null && !req.getChannel().isBlank())
                ? req.getChannel().trim().toUpperCase()
                : "LINK";

        String scopeRaw = (req != null && req.getScope() != null) ? req.getScope().trim().toUpperCase() : "";
        String shareScope = normalizeShareScope(scopeRaw, channel);

        String target = (req != null) ? req.getTarget() : null;

        // ✅ 로그 적재 (최초는 PENDING)
        ScheduleShareLog log = new ScheduleShareLog();
        log.setSchedule(schedule);
        log.setChannel(channel);
        log.setTarget(target);
        log.setStatus("PENDING");
        shareLogRepository.save(log);

        try {
            // ✅ shareCode 없으면 생성
            if (schedule.getShareCode() == null || schedule.getShareCode().isBlank()) {
                schedule.setShareCode(generateUniqueShareCode());
            }

            // ✅ 공유 ON + 공개범위 저장
            schedule.setShareEnabled("Y");
            schedule.setShareScope(shareScope);

            // 만료정책 필요시 사용
            // schedule.setShareExpiredAt(LocalDateTime.now().plusDays(7));

            scheduleRepository.save(schedule);

            // ✅ EMAIL 공유: Notification 적재만 하고, ShareLog는 PENDING 유지
            if ("EMAIL".equals(channel) && target != null && !target.isBlank()) {
                String baseUrl = (req != null && req.getBaseUrl() != null) ? req.getBaseUrl().trim() : "";
                String code = schedule.getShareCode();

                String body = baseUrl.isBlank()
                        ? ("공유 코드: " + code)
                        : ("일정 공유 링크: " + baseUrl + "/schedule/share/" + code + "\n공유 코드: " + code);

                Notification noti = new Notification();
                noti.setToEmail(target.trim());
                noti.setChannel("EMAIL");
                noti.setType("SCHEDULE_SHARED");
                noti.setTitle("[TRAVEL_LEAF] 일정 공유");
                noti.setBody(body);
                noti.setStatus("PENDING");

                // ✅ 느슨한 연결(추적용): noti -> share_log
                noti.setRelatedType("SCHEDULE_SHARE");
                noti.setRelatedId(log.getShareId());

                notificationRepository.save(noti);

            } else {
                // ✅ LINK 공유: 즉시 SENT + sentAt=now
                log.setStatus("SENT");
                log.setSentAt(LocalDateTime.now());
                shareLogRepository.save(log);
            }

            return new ShareResponse(schedule.getScheduleId(), schedule.getShareCode());

        } catch (Exception e) {
            log.setStatus("FAIL");
            String msg = rootMessage(e);
            if (msg != null && msg.length() > 1000) msg = msg.substring(0, 1000);
            log.setErrorMsg(msg);
            shareLogRepository.save(log);
            throw e;
        }
    }

    /**
     * ✅ 공유코드로 scheduleId 해석 (유효성/만료/활성 체크)
     * - PRIVATE/LINK/PUBLIC 상관없이 "공유 자체가 유효한지"만 체크
     */
    @Transactional(readOnly = true)
    public Long resolveSharedScheduleId(String code) {
        return mustSharedSchedule(code).getScheduleId();
    }

    /**
     * ✅ (추가) 비로그인 공유페이지 “조회” 허용 여부까지 포함
     * - PRIVATE면 비로그인 조회 금지(403)
     * - LINK/PUBLIC면 비로그인 조회 OK
     */
    @Transactional(readOnly = true)
    public Long resolveForPublicView(String code) {
        Schedule s = mustSharedSchedule(code);

        String scope = (s.getShareScope() == null) ? "" : s.getShareScope().trim().toUpperCase();
        if ("PRIVATE".equals(scope) || scope.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Login required for private share");
        }
        return s.getScheduleId();
    }

    /**
     * ✅ 내부 공통: shareCode 유효한 Schedule 반환
     */
    @Transactional(readOnly = true)
    public Schedule mustSharedSchedule(String code) {
        String c = (code == null) ? "" : code.trim();
        if (c.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is required");

        Schedule schedule = scheduleRepository.findTopByShareCodeOrderByScheduleIdDesc(c)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid share code"));

        if (!"Y".equalsIgnoreCase(schedule.getShareEnabled())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Share disabled");
        }

        if (schedule.getShareExpiredAt() != null && schedule.getShareExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Share expired");
        }

        return schedule;
    }

    /**
     * ✅ DB 제약조건에 맞는 SHARE_SCOPE로 변환
     * - DB 허용값: PRIVATE / PUBLIC / LINK
     */
    private String normalizeShareScope(String scopeRaw, String channel) {
        if ("PRIVATE".equals(scopeRaw) || "PUBLIC".equals(scopeRaw) || "LINK".equals(scopeRaw)) {
            return scopeRaw;
        }
        if ("LINK".equals(channel)) return "LINK";
        if ("EMAIL".equals(channel)) return "LINK";
        return "PRIVATE";
    }

    private String generateUniqueShareCode() {
        for (int i = 0; i < 50; i++) {
            String code = "SHR_" + randomBase62(12);
            if (!scheduleRepository.existsByShareCode(code)) return code;
        }
        return "SHR_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String randomBase62(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(BASE62.charAt(RND.nextInt(BASE62.length())));
        return sb.toString();
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur.getMessage() != null ? cur.getMessage() : t.getMessage();
    }
}
