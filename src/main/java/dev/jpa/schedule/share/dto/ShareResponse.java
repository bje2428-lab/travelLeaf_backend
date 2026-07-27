package dev.jpa.schedule.share.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShareResponse {
    private Long scheduleId;
    private String shareCode;
}
