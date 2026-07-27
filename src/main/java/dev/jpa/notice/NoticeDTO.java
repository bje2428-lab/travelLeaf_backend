package dev.jpa.notice;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {

    private Long noticeId;
    private String userId;
    private int grade;     // 사용자 권한 (0,1,2) — Controller에서 체크
    private String isFixed;
    private String title;
    private String content;
    private String fileUrl;
    private String category;
}
