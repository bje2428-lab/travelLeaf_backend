package dev.jpa.loginhistory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LoginHistoryDto {
    private Long loginHistoryNo;
    private String userid;
    private String nickname;
    private String name;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String userAgent;
}
