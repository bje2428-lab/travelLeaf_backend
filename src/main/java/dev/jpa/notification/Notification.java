package dev.jpa.notification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
    @SequenceGenerator(
            name = "notification_seq",
            sequenceName = "SEQ_NOTIFICATION",
            allocationSize = 1
    )
    @Column(name = "noti_id")
    private Long notiId;

    @Column(name = "user_id")
    private Long userId; // optional

    @Column(name = "to_email", length = 255)
    private String toEmail;

    @Column(name = "channel", length = 10, nullable = false)
    private String channel; // EMAIL

    @Column(name = "type", length = 30, nullable = false)
    private String type; // SCHEDULE_SHARED 등

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "body", length = 4000, nullable = false)
    private String body;

    @Column(name = "status", length = 10, nullable = false)
    private String status; // PENDING/SENT/FAIL

    // ERD 상 관계가 없으면 FK 없이 “느슨한 연결(추적용)”로만 사용
    @Column(name = "related_type", length = 20)
    private String relatedType; // SCHEDULE_SHARE 등

    @Column(name = "related_id")
    private Long relatedId; // share_log_id 등

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "fail_reason", length = 1000)
    private String failReason;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (channel == null) channel = "EMAIL";
    }

    public Notification() {}

    public Long getNotiId() { return notiId; }
    public void setNotiId(Long notiId) { this.notiId = notiId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
}
