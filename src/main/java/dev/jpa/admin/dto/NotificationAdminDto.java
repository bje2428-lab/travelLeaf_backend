// dev/jpa/admin/dto/NotificationAdminDto.java
package dev.jpa.admin.dto;

import java.time.LocalDateTime;

public class NotificationAdminDto {

    private Long notiId;

    // ✅ 숫자키(조인용)
    private Long userNo;

    // ✅ 화면표시용 USER_ID(문자열)
    private String userId;

    private String toEmail;
    private String channel;
    private String type;
    private String title;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    private String failReason;

    public NotificationAdminDto(
            Long notiId,
            Long userNo,
            String userId,
            String toEmail,
            String channel,
            String type,
            String title,
            String status,
            LocalDateTime createdAt,
            LocalDateTime sentAt,
            String failReason
    ) {
        this.notiId = notiId;
        this.userNo = userNo;
        this.userId = userId;
        this.toEmail = toEmail;
        this.channel = channel;
        this.type = type;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.failReason = failReason;
    }

    public Long getNotiId() { return notiId; }
    public Long getUserNo() { return userNo; }
    public String getUserId() { return userId; }

    public String getToEmail() { return toEmail; }
    public String getChannel() { return channel; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }

    public String getFailReason() { return failReason; }
}
