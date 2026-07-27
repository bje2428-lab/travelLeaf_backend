package dev.jpa.schedule.member;

import dev.jpa.schedule.Schedule;
import dev.jpa.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_MEMBER")
public class ScheduleMember {

    @EmbeddedId
    private ScheduleMemberId id;

    @MapsId("scheduleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_no", nullable = false)
    private User user;

    @Column(name = "role", nullable = false, length = 20)
    private String role;   // OWNER/EDITOR/VIEWER

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE/LEFT/BLOCKED

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected ScheduleMember() {}

    public ScheduleMember(Schedule schedule, User user, String role) {
        this.schedule = schedule;
        this.user = user;
        this.id = new ScheduleMemberId(schedule.getScheduleId(), user.getUserno());
        this.role = role;
        this.status = ScheduleMemberStatus.ACTIVE;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.joinedAt == null) this.joinedAt = now;
        this.updatedAt = now;

        if (this.status == null) this.status = ScheduleMemberStatus.ACTIVE;
        if (this.role == null) this.role = ScheduleMemberRole.VIEWER;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ScheduleMemberId getId() { return id; }
    public Schedule getSchedule() { return schedule; }
    public User getUser() { return user; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
