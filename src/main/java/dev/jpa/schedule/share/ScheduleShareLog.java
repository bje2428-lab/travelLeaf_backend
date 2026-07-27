// dev/jpa/schedule/share/ScheduleShareLog.java
package dev.jpa.schedule.share;

import dev.jpa.schedule.Schedule;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_SHARE_LOG")
public class ScheduleShareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "share_log_seq")
    @SequenceGenerator(
            name = "share_log_seq",
            sequenceName = "SEQ_SCHEDULE_SHARE_LOG",
            allocationSize = 1
    )
    @Column(name = "share_id")
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "channel", length = 10, nullable = false)
    private String channel; // LINK/EMAIL

    @Column(name = "target", length = 255)
    private String target;

    @Column(name = "status", length = 10, nullable = false)
    private String status;  // PENDING/SENT/FAIL

    @Column(name = "error_msg", length = 1000)
    private String errorMsg;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ 추가
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    protected ScheduleShareLog() {}

    public Long getShareId() { return shareId; }
    public void setShareId(Long shareId) { this.shareId = shareId; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ✅ 추가 getter/setter
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
