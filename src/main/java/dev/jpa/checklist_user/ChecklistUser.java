package dev.jpa.checklist_user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "CHECKLIST_USER")
public class ChecklistUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checklist_user_seq")
    @SequenceGenerator(
            name = "checklist_user_seq",
            sequenceName = "CHECKLIST_USER_SEQ",
            allocationSize = 1
    )
    @Column(name = "CHECK_ID")
    private Long checkId;

    /** FK: USER_TB(USER_NO) */
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;

    /** FK: CHECKLIST(ITEM_ID) */
    @Column(name = "ITEM_ID", nullable = false)
    private Long itemId;

    /** FK: CHECKLIST_BATCH(BATCH_ID) */
    @Column(name = "BATCH_ID", nullable = false)
    private Long batchId;

    /** 선택 시각 */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public ChecklistUser() {}

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ChecklistUser(Long userNo, Long itemId, Long batchId) {
        this.userNo = userNo;
        this.itemId = itemId;
        this.batchId = batchId;
        this.createdAt = LocalDateTime.now();
    }
}
