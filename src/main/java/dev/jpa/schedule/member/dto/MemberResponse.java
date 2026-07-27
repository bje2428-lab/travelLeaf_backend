// dev/jpa/schedule/member/dto/MemberResponse.java
package dev.jpa.schedule.member.dto;

import dev.jpa.schedule.member.ScheduleMember;

public class MemberResponse {
    private Long scheduleId;
    private Long userNo;
    private String role;
    private String status;

    public static MemberResponse fromEntity(ScheduleMember m) {
        MemberResponse r = new MemberResponse();
        r.scheduleId = m.getId().getScheduleId();
        r.userNo = m.getId().getUserNo();
        r.role = m.getRole();
        r.status = m.getStatus();
        return r;
    }

    public Long getScheduleId() { return scheduleId; }
    public Long getUserNo() { return userNo; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}
