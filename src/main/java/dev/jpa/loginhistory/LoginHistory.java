package dev.jpa.loginhistory;

import dev.jpa.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "LOGIN_HISTORY_TB")
@Getter
@Setter
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "login_history_seq")
    @SequenceGenerator(
        name = "login_history_seq",
        sequenceName = "LOGIN_HISTORY_SEQ",
        allocationSize = 1
    )
    @Column(name = "LOGIN_HISTORY_NO")
    private Long loginHistoryNo;

    // 유저 번호 FK → User 엔터티로 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", nullable = false, insertable = false, updatable = false)
    private User user;

    @Column(name = "USER_NO", nullable = false)
    private Long userno;

    @Column(name = "LOGIN_AT", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    @Column(name = "USER_AGENT", length = 200)
    private String userAgent;
}
