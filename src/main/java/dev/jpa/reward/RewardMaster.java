package dev.jpa.reward;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "REWARD_MASTER")
@Getter
@Setter
public class RewardMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reward_seq")
    @SequenceGenerator(
        name = "reward_seq",
        sequenceName = "REWARD_SEQ",
        allocationSize = 1
    )
    @Column(name = "REWARD_ID")
    private Long rewardId;

    @Column(name = "REWARD_TYPE")
    private String rewardType;   // EXP / POINT / BADGE

    @Column(name = "REWARD_VALUE")
    private Integer rewardValue;

    @Column(name = "REWARD_NAME")
    private String rewardName;

    @Column(name = "DESCRIPTION")
    private String description;
}
