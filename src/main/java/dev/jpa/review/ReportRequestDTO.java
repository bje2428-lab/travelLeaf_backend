package dev.jpa.review;

import lombok.Data;

@Data
public class ReportRequestDTO {
    private String userId;   // 신고자
    private String reason;   // SPAM / SWEAR / ...
    private String detail;   // 선택(상세설명)
}
