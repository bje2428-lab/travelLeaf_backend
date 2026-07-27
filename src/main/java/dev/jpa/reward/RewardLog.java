package dev.jpa.reward;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "REWARD_LOG")
@Getter
@Setter
public class RewardLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reward_log_seq")
    @SequenceGenerator(
        name = "reward_log_seq",
        sequenceName = "REWARD_LOG_SEQ",
        allocationSize = 1
    )
    private Long rewardLogId;

    @Column(nullable = false)
    private Long userNo;

    @Column(nullable = false)
    private Integer rewardValue;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private String sourceKey;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "REWARD_ID", nullable = false)
    private Long rewardId;

}
