// src/main/java/dev/jpa/schedule_detail/ScheduleDetailCont.java
package dev.jpa.schedule_detail;

import dev.jpa.schedule.member.ScheduleMemberService;
import dev.jpa.schedule.share.ScheduleShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/schedule/detail")
public class ScheduleDetailCont {

    private final ScheduleDetailService detailService;
    private final ScheduleMemberService memberService;
    private final ScheduleShareService shareService;

    public ScheduleDetailCont(
            ScheduleDetailService detailService,
            ScheduleMemberService memberService,
            ScheduleShareService shareService
    ) {
        this.detailService = detailService;
        this.memberService = memberService;
        this.shareService = shareService;
    }

    // =========================
    // ✅ 공통: 조회 권한 체크
    // - userNo 있으면: 멤버 권한 우선 시도
    //   -> 실패해도 shareCode가 있으면 shareCode로 fallback (프론트가 둘 다 보내는 케이스 방어)
    // - userNo 없고 shareCode 있으면: 공유 유효 + PRIVATE 차단 + scheduleId 매칭
    // =========================
    private void assertReadable(Long scheduleId, Long userNo, String shareCode) {
        String code = (shareCode == null) ? "" : shareCode.trim();

        // 1) 로그인 유저: 멤버 권한 먼저 시도
        if (userNo != null && userNo > 0) {
            try {
                memberService.assertCanView(scheduleId, userNo);
                return;
            } catch (ResponseStatusException ex) {
                // 멤버가 아니어도 shareCode가 있으면 공유권한으로 fallback
                if (code.isBlank()) throw ex;
            }
        }

        // 2) shareCode 기반 조회 (비로그인 포함)
        if (!code.isBlank()) {
            Long resolvedId = shareService.resolveForPublicView(code); // ✅ Long 반환 (PUBLIC/LINK만 OK, PRIVATE면 403)
            if (resolvedId == null || !resolvedId.equals(scheduleId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid share code for this schedule");
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userNo or shareCode is required");
    }

    /* =========================
       R: 특정 day 조회 (VIEW 가능)
       GET /schedule/detail/day/{scheduleId}/{dayNumber}?userNo=1
       GET /schedule/detail/day/{scheduleId}/{dayNumber}?shareCode=SHR_xxx
    ========================= */
    @GetMapping("/day/{scheduleId}/{dayNumber}")
    public ResponseEntity<List<ScheduleDetailDTO.Response>> getDay(
            @PathVariable("scheduleId") Long scheduleId,
            @PathVariable("dayNumber") Integer dayNumber,
            @RequestParam(value = "userNo", required = false) Long userNo,
            @RequestParam(value = "shareCode", required = false) String shareCode
    ) {
        assertReadable(scheduleId, userNo, shareCode);
        return ResponseEntity.ok(detailService.getDay(scheduleId, dayNumber));
    }

    /* =========================
       U: 특정 day 덮어쓰기 (EDITOR/OWNER)
       PUT /schedule/detail/day/{scheduleId}/{dayNumber}?userNo=1
       (편집은 shareCode 허용 안 함)
    ========================= */
    @PutMapping("/day/{scheduleId}/{dayNumber}")
    public ResponseEntity<List<ScheduleDetailDTO.Response>> replaceDay(
            @PathVariable("scheduleId") Long scheduleId,
            @PathVariable("dayNumber") Integer dayNumber,
            @RequestParam("userNo") Long userNo,
            @RequestBody List<ScheduleDetailDTO.CreateRequest> rows
    ) {
        memberService.assertCanEdit(scheduleId, userNo);
        return ResponseEntity.ok(detailService.replaceDay(scheduleId, dayNumber, rows));
    }

    /* =========================
       C: 상세 일정 단건 생성 (EDITOR/OWNER)
       POST /schedule/detail/save?userNo=1
    ========================= */
    @PostMapping("/save")
    public ResponseEntity<ScheduleDetailDTO.Response> createDetail(
            @RequestParam("userNo") Long userNo,
            @RequestBody ScheduleDetailDTO.CreateRequest request
    ) {
        Long scheduleId = request.getScheduleId();
        memberService.assertCanEdit(scheduleId, userNo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(detailService.createDetail(request));
    }

    /* =========================
       C: AI 일정 전체 저장 (EDITOR/OWNER)
       POST /schedule/detail/save/ai?userNo=1&scheduleId=10
    ========================= */
    @PostMapping("/save/ai")
    public ResponseEntity<Void> saveAiDetails(
            @RequestParam("userNo") Long userNo,
            @RequestParam("scheduleId") Long scheduleId,
            @RequestBody List<ScheduleDetailDTO.CreateRequest> requests
    ) {
        memberService.assertCanEdit(scheduleId, userNo);
        detailService.saveAiDetails(scheduleId, requests);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* =========================
       R: 일정 상세 전체 조회 (VIEW 가능)
       GET /schedule/detail/list/{scheduleId}?userNo=1
       GET /schedule/detail/list/{scheduleId}?shareCode=SHR_xxx
    ========================= */
    @GetMapping("/list/{scheduleId}")
    public ResponseEntity<List<ScheduleDetailDTO.Response>> getDetails(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam(value = "userNo", required = false) Long userNo,
            @RequestParam(value = "shareCode", required = false) String shareCode
    ) {
        assertReadable(scheduleId, userNo, shareCode);
        return ResponseEntity.ok(detailService.getDetailsBySchedule(scheduleId));
    }

    /* =========================
       R: 상세 단건 조회 (VIEW 가능)
       GET /schedule/detail/{detailId}?scheduleId=10&userNo=1
       GET /schedule/detail/{detailId}?scheduleId=10&shareCode=SHR_xxx
    ========================= */
    @GetMapping("/{detailId}")
    public ResponseEntity<ScheduleDetailDTO.Response> getDetail(
            @PathVariable("detailId") Long detailId,
            @RequestParam("scheduleId") Long scheduleId,
            @RequestParam(value = "userNo", required = false) Long userNo,
            @RequestParam(value = "shareCode", required = false) String shareCode
    ) {
        assertReadable(scheduleId, userNo, shareCode);
        return ResponseEntity.ok(detailService.getDetail(detailId));
    }

    /* =========================
       D: 상세 삭제 (EDITOR/OWNER)
       DELETE /schedule/detail/{detailId}?scheduleId=10&userNo=1
    ========================= */
    @DeleteMapping("/{detailId}")
    public ResponseEntity<String> deleteDetail(
            @PathVariable("detailId") Long detailId,
            @RequestParam("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        memberService.assertCanEdit(scheduleId, userNo);
        detailService.deleteDetail(detailId);

        return ResponseEntity.ok("상세 일정이 삭제되었습니다. detailId=" + detailId);
    }
}
