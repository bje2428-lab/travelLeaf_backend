// dev/jpa/schedule/member/ScheduleMemberCont.java
package dev.jpa.schedule.member;

import dev.jpa.schedule.member.dto.JoinRequest;
import dev.jpa.schedule.member.dto.JoinResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleMemberCont {

    private final ScheduleMemberService memberService;

    public ScheduleMemberCont(ScheduleMemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * ✅ POST /api/schedule/share/{code}/join
     * - body: { "userNo": 1 }
     * - 또는 query: ?userNo=1  (팀원/환경 차이 대비)
     */
    @PostMapping("/share/{code}/join")
    public ResponseEntity<JoinResponse> join(
            @PathVariable("code") String code,
            @RequestParam(value = "userNo", required = false) Long userNoParam,
            @RequestBody(required = false) JoinRequest req
    ) {
        Long userNo = (userNoParam != null) ? userNoParam : (req != null ? req.getUserNo() : null);

        if (userNo == null || userNo <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "userNo is required"
            );
        }

        return ResponseEntity.ok(memberService.joinByShareCode(code, userNo));
    }
}
