package dev.jpa.review;

import java.sql.Timestamp;

public interface ReviewCommentReportRow {
    Long getReportId();
    String getReporterId();
    String getManagerId();     // USER_ID 컬럼
    String getReportCategory();
    String getReason();
    String getEvidenceUrl();
    String getStatus();
    Double getAiScore();
    String getAiModel();
    String getAiDetected();
    Timestamp getCreatedAt();
    Timestamp getProcessedAt();
    Long getCommentId();
}
