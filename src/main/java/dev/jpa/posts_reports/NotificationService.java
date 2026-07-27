package dev.jpa.posts_reports;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendReportProcessed(String reporterId, Long reportId, String status) {
        System.out.println("📢 신고자 [" + reporterId + "] 신고 처리됨 => "
                + "reportId=" + reportId + ", status=" + status);
    }

    public void notifyAdmins(String message) {
        System.out.println("📢 관리자 알림 => " + message);
    }
}
