// dev/jpa/schedule/share/dto/ShareLogResponse.java
package dev.jpa.schedule.share.dto;

import dev.jpa.schedule.share.ScheduleShareLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShareLogResponse {
    private Long shareId;
    private Long scheduleId;
    private String channel;
    private String target;
    private String status;
    private String errorMsg;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public static ShareLogResponse from(ScheduleShareLog log) {
        return new ShareLogResponse(
                log.getShareId(),
                log.getSchedule().getScheduleId(),
                log.getChannel(),
                log.getTarget(),
                log.getStatus(),
                log.getErrorMsg(),
                log.getCreatedAt(),
                log.getSentAt()
        );
    }
}
