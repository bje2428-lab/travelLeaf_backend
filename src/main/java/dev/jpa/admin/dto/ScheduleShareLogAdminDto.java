package dev.jpa.admin.dto;

import java.time.LocalDateTime;

public class ScheduleShareLogAdminDto {

    private Long shareId;
    private Long scheduleId;
    private String scheduleTitle;
    private String userId;

    private String channel;
    private String target;
    private String status;
    private String errorMsg;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public ScheduleShareLogAdminDto(
            Long shareId,
            Long scheduleId,
            String scheduleTitle,
            String userId,
            String channel,
            String target,
            String status,
            String errorMsg,
            LocalDateTime createdAt,
            LocalDateTime sentAt
    ) {
        this.shareId = shareId;
        this.scheduleId = scheduleId;
        this.scheduleTitle = scheduleTitle;
        this.userId = userId;
        this.channel = channel;
        this.target = target;
        this.status = status;
        this.errorMsg = errorMsg;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public Long getShareId() { return shareId; }
    public Long getScheduleId() { return scheduleId; }
    public String getScheduleTitle() { return scheduleTitle; }
    public String getUserId() { return userId; }

    public String getChannel() { return channel; }
    public String getTarget() { return target; }
    public String getStatus() { return status; }
    public String getErrorMsg() { return errorMsg; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
}
