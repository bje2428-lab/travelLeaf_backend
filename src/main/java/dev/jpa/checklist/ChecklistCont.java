package dev.jpa.checklist;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/checklist")
public class ChecklistCont {

  @Autowired
  private ChecklistService checklistService;

  public ChecklistCont() {
      System.out.println("-> ChecklistController created.");
  }

  /** 등록
   * http://localhost:9100/checklist/save
   * @param checklistDTO
   * @return
   * 
   * Postman 예시
   * {
    "category" : "Mood",
    "itemName" : "조용한",
    "description" : "한적하고 편안한 여행을 선호",
    "createdAt" : "2025-10-27 16:42:30"
    }
   */
  @PostMapping("/save")
  public ResponseEntity<?> save(@RequestBody ChecklistDTO checklistDTO) {
      try {
          Checklist saved = checklistService.save(checklistDTO);
          return ResponseEntity.ok(saved);
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(500).body(e.getMessage());
      }
  }

  /** 전체 조회
   * http://localhost:9100/checklist/find_all
   * @return
   */
  @GetMapping("/find_all")
  public ResponseEntity<List<Checklist>> findAll() {
      return ResponseEntity.ok(checklistService.findAll());
  }

  /** 카테고리별 조회
   * http://localhost:9100/checklist//category/Mood
   * @param category
   * @return
   */
  @GetMapping("/category/{category}")
  public ResponseEntity<List<Checklist>> findByCategory(@PathVariable("category") String category) {
      return ResponseEntity.ok(checklistService.findByCategory(category));
  }

  /** 단건 조회
   * http://localhost:9100/checklist/read/1
   * @param itemId
   * @return
   */
  @GetMapping("/read/{itemId}")
  public ResponseEntity<Checklist> read(@PathVariable("itemId") long itemId) {
      Checklist item = checklistService.findByItemId(itemId);
      return ResponseEntity.ok(item);
  }

  /** 수정
   * http://localhost:9100/checklist/update
   * @param checklistDTO
   * @return
   */
  @PostMapping("/update")
  public ResponseEntity<Integer> update(@RequestBody ChecklistDTO checklistDTO) {
      int cnt = checklistService.update(checklistDTO);
      return ResponseEntity.ok(cnt);
  }

  /** 삭제
   * http://localhost:9100/checklist/delete/4
   * @param itemId
   * @return
   */
  @DeleteMapping("/delete/{itemId}")
  public ResponseEntity<Integer> delete(@PathVariable("itemId") long itemId) {
      Optional<Checklist> item = checklistService.findById(itemId);
      int cnt = 0;

      if (item.isPresent()) {
          cnt = checklistService.delete(itemId);
      } else {
          cnt = 2;
      }

      return ResponseEntity.ok(cnt);
  }
}