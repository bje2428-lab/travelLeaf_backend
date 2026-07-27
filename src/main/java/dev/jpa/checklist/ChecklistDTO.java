package dev.jpa.checklist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChecklistDTO {

  private Long itemId;

  private String category;

  private String itemName;

  private String description;

  private String createdAt;

  public Checklist toEntity() {
    Checklist entity = new Checklist();
    entity.setItemId(itemId);
    entity.setCategory(category);
    entity.setItemName(itemName);
    entity.setDescription(description);
    entity.setCreatedAt(createdAt);
    return entity;
  }

}
