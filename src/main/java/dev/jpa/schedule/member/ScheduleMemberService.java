package dev.jpa.schedule.member;

import dev.jpa.schedule.Schedule;
import dev.jpa.schedule.ScheduleRepository;
import dev.jpa.schedule.share.ScheduleShareService;
import dev.jpa.schedule.member.dto.JoinResponse;
import dev.jpa.user.User;
import dev.jpa.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ScheduleMemberService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScheduleMemberRepository memberRepository;
    private final ScheduleShareService shareService;

    public ScheduleMemberService(
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            ScheduleMemberRepository memberRepository,
            ScheduleShareService shareService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.shareService = shareService;
    }

    /** ✅ 공유코드로 참여(가입) */
    @Transactional
    public JoinResponse joinByShareCode(String code, Long userNo) {
        if (userNo == null || userNo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userNo is required");
        }
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is required");
        }

        String trimmedCode = code.trim();

        // ✅ 여기서 shareEnabled/만료/유효성 체크까지 끝남
        Long scheduleId = shareService.resolveSharedScheduleId(trimmedCode);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Schedule not found: " + scheduleId));

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + userNo));

        Long ownerNo = (schedule.getUser() != null) ? schedule.getUser().getUserno() : null;

        // =========================
        // ✅ 핵심 수정: role 결정
        // - DB shareScope 허용값: PRIVATE / PUBLIC / LINK
        // - LINK 공유 = 협업 편집 허용(EDITOR)
        // - PUBLIC/PRIVATE 는 기본 VIEWER
        // =========================
        String role;
        if (ownerNo != null && ownerNo.equals(userNo)) {
            role = ScheduleMemberRole.OWNER;
        } else {
            String shareScope = (schedule.getShareScope() == null) ? "" : schedule.getShareScope().trim().toUpperCase();

            boolean editableByLink = "LINK".equals(shareScope); // ✅ 최소 수정 포인트
            role = editableByLink ? ScheduleMemberRole.EDITOR : ScheduleMemberRole.VIEWER;
        }

        ScheduleMember m = memberRepository.findByIdScheduleIdAndIdUserNo(scheduleId, userNo).orElse(null);

        if (m == null) {
            // 신규 가입
            m = new ScheduleMember(schedule, user, role);
        } else {
            // 기존 가입자 재참여
            if (ScheduleMemberStatus.BLOCKED.equals(m.getStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Blocked member");
            }

            m.setStatus(ScheduleMemberStatus.ACTIVE);

            // ✅ 권한 업그레이드만 허용 (기존 EDITOR를 VIEWER로 내리진 않음)
            if (ScheduleMemberRole.EDITOR.equals(role) && ScheduleMemberRole.VIEWER.equals(m.getRole())) {
                m.setRole(ScheduleMemberRole.EDITOR);
            }

            // OWNER는 항상 유지
            if (ScheduleMemberRole.OWNER.equals(role)) {
                m.setRole(ScheduleMemberRole.OWNER);
            }
        }

        memberRepository.save(m);
        return new JoinResponse(scheduleId, userNo, m.getRole(), m.getStatus());
    }

    /** ✅ 조회 권한: OWNER or ACTIVE 멤버 */
    @Transactional(readOnly = true)
    public void assertCanView(Long scheduleId, Long userNo) {
        if (userNo == null || userNo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userNo is required");
        }

        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Long ownerNo = (s.getUser() != null) ? s.getUser().getUserno() : null;
        if (ownerNo != null && ownerNo.equals(userNo)) return;

        boolean ok = memberRepository.existsByIdScheduleIdAndIdUserNoAndStatus(
                scheduleId, userNo, ScheduleMemberStatus.ACTIVE
        );
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member");
    }

    /** ✅ 편집 권한: OWNER or ACTIVE + (OWNER/EDITOR) */
    @Transactional(readOnly = true)
    public void assertCanEdit(Long scheduleId, Long userNo) {
        if (userNo == null || userNo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userNo is required");
        }

        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Long ownerNo = (s.getUser() != null) ? s.getUser().getUserno() : null;
        if (ownerNo != null && ownerNo.equals(userNo)) return;

        ScheduleMember m = memberRepository.findByIdScheduleIdAndIdUserNo(scheduleId, userNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member"));

        if (!ScheduleMemberStatus.ACTIVE.equals(m.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Member not active");
        }
        if (!ScheduleMemberRole.canEdit(m.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Read-only member");
        }
    }

    /** ✅ OWNER만 체크(삭제 등에 사용) */
    @Transactional(readOnly = true)
    public void assertIsOwner(Long scheduleId, Long userNo) {
        if (userNo == null || userNo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userNo is required");
        }

        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Long ownerNo = (s.getUser() != null) ? s.getUser().getUserno() : null;
        if (ownerNo == null || !ownerNo.equals(userNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner only");
        }
    }

    @Transactional(readOnly = true)
    public List<ScheduleMember> listActiveMembers(Long scheduleId) {
        return memberRepository.findByIdScheduleIdAndStatusOrderByJoinedAtAsc(
                scheduleId, ScheduleMemberStatus.ACTIVE
        );
    }

    /** ✅ 나가기 */
    @Transactional
    public void leave(Long scheduleId, Long userNo) {
        if (userNo == null || userNo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userNo is required");
        }

        ScheduleMember m = memberRepository.findByIdScheduleIdAndIdUserNo(scheduleId, userNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (ScheduleMemberRole.OWNER.equals(m.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot leave");
        }

        m.setStatus(ScheduleMemberStatus.LEFT);
        memberRepository.save(m);
    }

    /** ✅ 일정 생성 직후: 소유자를 OWNER 멤버로 자동등록 */
    @Transactional
    public void ensureOwnerMember(Schedule schedule) {
        if (schedule == null || schedule.getScheduleId() == null) return;
        if (schedule.getUser() == null || schedule.getUser().getUserno() == null) return;

        Long scheduleId = schedule.getScheduleId();
        Long ownerNo = schedule.getUser().getUserno();

        ScheduleMember existing = memberRepository.findByIdScheduleIdAndIdUserNo(scheduleId, ownerNo).orElse(null);
        if (existing == null) {
            memberRepository.save(new ScheduleMember(schedule, schedule.getUser(), ScheduleMemberRole.OWNER));
        } else {
            existing.setStatus(ScheduleMemberStatus.ACTIVE);
            existing.setRole(ScheduleMemberRole.OWNER);
            memberRepository.save(existing);
        }
    }
}
