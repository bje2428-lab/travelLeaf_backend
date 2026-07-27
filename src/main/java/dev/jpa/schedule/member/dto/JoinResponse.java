package dev.jpa.schedule.member.dto;

public class JoinResponse {
    private Long scheduleId;
    private Long userNo;
    private String role;
    private String status;

    public JoinResponse() {}

    public JoinResponse(Long scheduleId, Long userNo, String role, String status) {
        this.scheduleId = scheduleId;
        this.userNo = userNo;
        this.role = role;
        this.status = status;
    }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getUserNo() { return userNo; }
    public void setUserNo(Long userNo) { this.userNo = userNo; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
