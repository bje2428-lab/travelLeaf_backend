package dev.jpa.admin;

import dev.jpa.admin.dto.NotificationAdminDto;
import dev.jpa.notification.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminNotificationCont {

    private final NotificationRepository notificationRepository;

    public AdminNotificationCont(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ✅ GET /admin/notifications?q=&date=YYYY-MM-DD
    @GetMapping("/notifications")
    public List<NotificationAdminDto> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "date", required = false) String date
    ) {
        String qq = (q == null) ? null : q.trim();

        LocalDateTime from = null;
        LocalDateTime to = null;

        if (date != null && !date.isBlank()) {
            LocalDate d = LocalDate.parse(date);
            from = d.atStartOfDay();
            to = d.plusDays(1).atStartOfDay();
        }

        return notificationRepository.findAdminList(qq, from, to);
    }

    // ✅ DELETE /admin/notifications/{notiId}
    @DeleteMapping("/notifications/{notiId}")
    public void delete(@PathVariable("notiId") Long notiId) {
        notificationRepository.deleteById(notiId);
    }
}
