// dev/jpa/schedule/ScheduleCont.java
package dev.jpa.schedule;

import dev.jpa.schedule.member.ScheduleMemberService;
import dev.jpa.schedule.share.ScheduleShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleCont {

    private final ScheduleService scheduleService;
    private final ScheduleMemberService memberService;
    private final ScheduleShareService shareService;

    public ScheduleCont(
            ScheduleService scheduleService,
            ScheduleMemberService memberService,
            ScheduleShareService shareService
    ) {
        this.scheduleService = scheduleService;
        this.memberService = memberService;
        this.shareService = shareService;
    }

    /* =========================
       C: 생성
    ========================= */
    @PostMapping("/save")
    public ResponseEntity<ScheduleDTO.Response> create(
            @RequestBody ScheduleDTO.CreateRequest req
    ) {
        ScheduleDTO.Response res = scheduleService.createSchedule(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /* =========================
       R: 단건 조회
    ========================= */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO.Response> get(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam(value = "userNo", required = false) Long userNo,
            @RequestParam(value = "shareCode", required = false) String shareCode
    ) {
        // ✅ 공유 링크 진입
        if (shareCode != null && !shareCode.isBlank()) {

            Long resolved;
            if (userNo != null && userNo > 0) {
                resolved = shareService.resolveSharedScheduleId(shareCode);
            } else {
                // 비로그인 → PUBLIC만 허용
                resolved = shareService.resolveForPublicView(shareCode);
            }

            if (!scheduleId.equals(resolved)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // 로그인 유저: 멤버 등록 후 조회
            if (userNo != null && userNo > 0) {
                memberService.joinByShareCode(shareCode, userNo);
                return ResponseEntity.ok(
                        scheduleService.getSchedule(scheduleId, userNo)
                );
            }

            // 비로그인 유저: 공유 전용 조회
            return ResponseEntity.ok(
                    scheduleService.getScheduleByShare(scheduleId)
            );
        }

        // ✅ 일반 진입
        return ResponseEntity.ok(
                scheduleService.getSchedule(scheduleId, userNo)
        );
    }

    /* =========================
       U: 수정
    ========================= */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO.Response> update(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo,
            @RequestParam(value = "shareCode", required = false) String shareCode,
            @RequestBody ScheduleDTO.UpdateRequest req
    ) {
        if (shareCode != null && !shareCode.isBlank()) {
            Long resolved = shareService.resolveSharedScheduleId(shareCode);
            if (!scheduleId.equals(resolved)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            memberService.joinByShareCode(shareCode, userNo);
        }

        memberService.assertCanEdit(scheduleId, userNo);
        ScheduleDTO.Response res = scheduleService.updateSchedule(scheduleId, req);
        return ResponseEntity.ok(res);
    }

    // 호환: /schedule/update/{id}
    @PutMapping("/update/{scheduleId}")
    public ResponseEntity<ScheduleDTO.Response> updateCompat(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo,
            @RequestParam(value = "shareCode", required = false) String shareCode,
            @RequestBody ScheduleDTO.UpdateRequest req
    ) {
        return update(scheduleId, userNo, shareCode, req);
    }

    /* =========================
       R: 내 스케줄 목록
    ========================= */
    @GetMapping("/list")
    public ResponseEntity<List<ScheduleDTO.Response>> listMine(
            @RequestParam("userNo") Long userNo
    ) {
        return ResponseEntity.ok(scheduleService.listMine(userNo));
    }

    // 호환: /schedule/user/{userNo}
    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<ScheduleDTO.Response>> listMine2(
            @PathVariable("userNo") Long userNo
    ) {
        return ResponseEntity.ok(scheduleService.listMine(userNo));
    }

    /* =========================
       D: 삭제 (OWNER만)
    ========================= */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> delete(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        memberService.assertIsOwner(scheduleId, userNo);
        scheduleService.deleteSchedule(scheduleId, userNo); // ✅ userNo 포함
        return ResponseEntity.noContent().build();
    }

    // 호환: /schedule/delete/{id}
    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<Void> deleteCompat(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        return delete(scheduleId, userNo);
    }
}
