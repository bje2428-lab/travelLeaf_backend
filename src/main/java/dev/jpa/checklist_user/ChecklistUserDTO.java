package dev.jpa.checklist_user;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChecklistUserDTO {

    /** 체크 ID (PK) — UPDATE 시 필요, INSERT 시 null */
    private Long checkId;

    /** 사용자 번호 */
    private Long userNo;

    /** 체크된 ITEM 번호 */
    private Long itemId;

    /** CHECKLIST_BATCH 기준 batchId */
    private Long batchId;

    /** DTO → Entity 변환 */
    public ChecklistUser toEntity() {
        return new ChecklistUser(
            userNo,
            itemId,
            batchId
        );
    }

    /** INSERT 용 생성자 */
    public ChecklistUserDTO(Long userNo, Long itemId, Long batchId) {
        this.checkId = null;
        this.userNo = userNo;
        this.itemId = itemId;
        this.batchId = batchId;
    }
}
