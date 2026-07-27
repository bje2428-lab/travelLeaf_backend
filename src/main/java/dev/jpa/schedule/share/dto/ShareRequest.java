package dev.jpa.schedule.share.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ShareRequest {
    private String channel; // LINK, EMAIL
    private String target;  // EMAIL일 때 수신자
    private String scope;   // VIEW or EDIT
    private String baseUrl; // 이메일 본문용(옵션)
}
