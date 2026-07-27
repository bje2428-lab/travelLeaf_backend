package dev.jpa.checklist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Checklist {

  /** 체크리스트 항목 ID */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checklist_seq")
  @SequenceGenerator(name = "checklist_seq", sequenceName = "CHECKLIST_SEQ", allocationSize = 1)
  private Long itemId;

  /** 항목 카테고리 */
  private String category;

  /** 항목 이름 */
  private String itemName;

  /** 항목 설명 */
  private String description;

  /** 등록일 */
  private String createdAt;

  public Checklist() {
    
  }

  public Checklist(String category, String itemName, String description, String createdAt) {
      this.category = category;
      this.itemName = itemName;
      this.description = description;
      this.createdAt = createdAt;
  }
}
