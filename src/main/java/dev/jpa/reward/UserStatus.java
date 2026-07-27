package dev.jpa.reward;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "USER_STATUS")
@Getter
@Setter
public class UserStatus {

    @Id
    @Column(name = "USER_NO")
    private Long userNo;

    @Column(name = "CURRENT_EXP")
    private Integer currentExp = 0;

    @Column(name = "CURRENT_LEVEL")
    private Integer currentLevel = 1;

    @Column(name = "CURRENT_POINT")
    private Integer currentPoint = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT")
    private Date updatedAt = new Date();
    
    @Column(name = "LAST_NOTIFIED_LEVEL")
    private Integer lastNotifiedLevel = 0;

}
