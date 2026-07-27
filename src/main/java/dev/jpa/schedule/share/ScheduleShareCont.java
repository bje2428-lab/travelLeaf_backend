// dev/jpa/schedule/share/ScheduleShareCont.java
package dev.jpa.schedule.share;

import dev.jpa.schedule.Schedule;
import dev.jpa.schedule.ScheduleRepository;
import dev.jpa.schedule.share.dto.ShareLogResponse;
import dev.jpa.schedule.share.dto.ShareRequest;
import dev.jpa.schedule.share.dto.ShareResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleShareCont {

    private final ScheduleShareService scheduleShareService;
    private final ScheduleShareLogRepository shareLogRepository;
    private final ScheduleRepository scheduleRepository;

    public ScheduleShareCont(
            ScheduleShareService scheduleShareService,
            ScheduleShareLogRepository shareLogRepository,
            ScheduleRepository scheduleRepository
    ) {
        this.scheduleShareService = scheduleShareService;
        this.shareLogRepository = shareLogRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // ✅ 공유 생성/갱신
    @PostMapping("/{scheduleId}/share")
    public ResponseEntity<ShareResponse> share(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody(required = false) ShareRequest req
    ) {
        return ResponseEntity.ok(scheduleShareService.shareSchedule(scheduleId, req));
    }

    // ✅ 공유코드 -> scheduleId
    @GetMapping("/share/{code}")
    public ResponseEntity<Long> resolve(@PathVariable("code") String code) {
        return ResponseEntity.ok(scheduleShareService.resolveSharedScheduleId(code));
    }

    // =========================
    // ✅ 공유 기록 목록 조회 (OWNER만)
    // GET /api/schedule/{scheduleId}/share-logs?userNo=19
    // =========================
    @GetMapping("/{scheduleId}/share-logs")
    public ResponseEntity<List<ShareLogResponse>> logs(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Long ownerNo = schedule.getUser() != null ? schedule.getUser().getUserno() : null;
        if (ownerNo == null || !ownerNo.equals(userNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can view logs");
        }

        List<ShareLogResponse> res = shareLogRepository
                .findBySchedule_ScheduleIdOrderByCreatedAtDesc(scheduleId)
                .stream()
                .map(ShareLogResponse::from)
                .toList();

        return ResponseEntity.ok(res);
    }

    // =========================
    // ✅ 공유 기록 삭제 (OWNER만)
    // DELETE /api/schedule/{scheduleId}/share-logs/{shareId}?userNo=19
    // =========================
    @DeleteMapping("/{scheduleId}/share-logs/{shareId}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable("scheduleId") Long scheduleId,
            @PathVariable("shareId") Long shareId,
            @RequestParam("userNo") Long userNo
    ) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Long ownerNo = schedule.getUser() != null ? schedule.getUser().getUserno() : null;
        if (ownerNo == null || !ownerNo.equals(userNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can delete logs");
        }

        ScheduleShareLog log = shareLogRepository.findById(shareId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log not found"));

        if (!log.getSchedule().getScheduleId().equals(scheduleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Log is not in this schedule");
        }

        shareLogRepository.delete(log);
        return ResponseEntity.noContent().build();
    }

    // ❌ join은 ScheduleMemberCont에 이미 있으니 여기서 제거!
}
