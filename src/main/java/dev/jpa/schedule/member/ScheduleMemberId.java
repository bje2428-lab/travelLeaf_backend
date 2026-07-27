package dev.jpa.schedule.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScheduleMemberId implements Serializable {

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "user_no")
    private Long userNo;

    protected ScheduleMemberId() {}

    public ScheduleMemberId(Long scheduleId, Long userNo) {
        this.scheduleId = scheduleId;
        this.userNo = userNo;
    }

    public Long getScheduleId() { return scheduleId; }
    public Long getUserNo() { return userNo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleMemberId that)) return false;
        return Objects.equals(scheduleId, that.scheduleId) && Objects.equals(userNo, that.userNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, userNo);
    }
}
