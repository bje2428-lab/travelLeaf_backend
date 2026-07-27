package dev.jpa.comments_reports;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsReportDTO {

    private Long reportId;
    private String reporterId;
    private String reportCategory;
    private String reason;
    private String evidenceUrl;
    private String status;
    private Long commentId;
}
