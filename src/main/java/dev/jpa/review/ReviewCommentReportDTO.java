package dev.jpa.review;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentReportDTO {

    private Long reportId;

    @JsonAlias({"reporter_id", "reporterId"})
    private String reporterId;

    @JsonAlias({"report_category", "reportCategory"})
    private String reportCategory;

    @JsonAlias({"reason"})
    private String reason;

    @JsonAlias({"evidence_url", "evidenceUrl"})
    private String evidenceUrl;

    @JsonAlias({"status"})
    private String status;

    @JsonAlias({"comment_id", "commentId"})
    private Long commentId;
}
