package dev.jpa.admin;

import dev.jpa.admin.dto.ScheduleShareLogAdminDto;
import dev.jpa.schedule.share.ScheduleShareLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminScheduleShareLogCont {

    private final ScheduleShareLogRepository shareLogRepository;

    public AdminScheduleShareLogCont(ScheduleShareLogRepository shareLogRepository) {
        this.shareLogRepository = shareLogRepository;
    }

    // ✅ GET /admin/schedule-share-logs?q=&date=
    @GetMapping("/schedule-share-logs")
    public List<ScheduleShareLogAdminDto> list(
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

        return shareLogRepository.findAdminList(qq, from, to);
    }

    // ✅ DELETE /admin/schedule-share-logs/{shareId}
    @DeleteMapping("/schedule-share-logs/{shareId}")
    public ResponseEntity<Void> delete(@PathVariable("shareId") Long shareId) {
        if (shareId == null) return ResponseEntity.badRequest().build();
        if (!shareLogRepository.existsById(shareId)) {
            return ResponseEntity.noContent().build();
        }
        shareLogRepository.deleteById(shareId);
        return ResponseEntity.noContent().build();
    }
}
