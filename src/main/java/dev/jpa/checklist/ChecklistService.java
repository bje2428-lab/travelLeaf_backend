package dev.jpa.checklist;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.tool.Tool;
import jakarta.transaction.Transactional;

@Service
public class ChecklistService {

  @Autowired
  private ChecklistRepository checklistRepository;

  public ChecklistService() {
      System.out.println("-> ChecklistService created");
  }

  /** 등록 */
  public Checklist save(ChecklistDTO checklistDTO) {
    checklistDTO.setCreatedAt(Tool.getDate());
      Checklist saved = checklistRepository.save(checklistDTO.toEntity());
      System.out.println("-> itemId: " + saved.getItemId());
      return saved;
  }

  /** 전체 조회 (카테고리 > ITEM_ID 오름차순) */
  public List<Checklist> findAll() {
      List<Checklist> list = checklistRepository.findAllByOrderByCategoryAscItemIdAsc();
      return list;
  }

  /** 카테고리별 조회 */
  public List<Checklist> findByCategory(String category) {
      List<Checklist> list = checklistRepository.findByCategoryOrderByItemIdAsc(category);
      return list;
  }

  /** 단건 조회 */
  public Checklist findByItemId(long itemId) {
      Checklist item = checklistRepository.findById(itemId).orElse(null);
      return item;
  }

  /** Optional 조회 */
  public Optional<Checklist> findById(Long itemId) {
      return checklistRepository.findById(itemId);
  }

  /** 수정 */
  @Transactional
  public int update(ChecklistDTO dto) {

      int updatedCnt = checklistRepository.update(
              dto.getCategory(),
              dto.getItemName(),
              dto.getDescription(),
              dto.getCreatedAt(),
              dto.getItemId()
      );

      return updatedCnt;
  }

  /** 삭제 */
  public int delete(long itemId) {
      int cnt = 0;

      try {
          checklistRepository.deleteById(itemId);
          cnt = 1;
      } catch (Exception e) {
          System.out.println(e.toString());
      }

      return cnt;
  }
}