package dev.jpa.cate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cate")
public class CateCont {
  @Autowired
  private CateService cateService;
  
  public CateCont() {
    System.out.println("-> CateCont created.");
    
  }

  /**
   * 등록, http://localhost:9100/cate/save
   * 
   * @param issueDTO
   * @return
   */
  @PostMapping(path = "/save")
  public ResponseEntity<Cate> save(@RequestBody CateDTO cateDTO) {
    System.out.println("-> cateDTO: " + cateDTO);
    
    Cate savedEntity = cateService.save(cateDTO);

    return ResponseEntity.ok(savedEntity);
  }
  
  /**
   * 전체 목록, http://localhost:9100/cate/find_all
   * 
   * @return
   */
  @GetMapping(path = "/find_all")
  public List<Cate> findAll() {
    // List<Cate> list = cateService.findAllByOrderByGrpAscNameAsc();
    List<Cate> list = cateService.findAllByOrderBySeqnoAsc();

    return list;
  }
  
  /**
   * 조회 + 조회수 증가, Primary Key를 이용한 조회 
   * http://localhost:9100/cate/9
   * 
   * @return
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<Cate> findByIdRead(@PathVariable("pk") long pk) {
      Cate cate = cateService.findById(pk); // 조회수 반영
      
      if (cate != null) {
        return ResponseEntity.ok(cate);
      } else {
        return ResponseEntity.notFound().build(); // 404
      }
  }
  
  /**
   * 수정, http://localhost:9100/cate/update
   * 
   * @param cateDTO
   * @return
   */
  @PutMapping(path = "/update")
  public ResponseEntity<Cate> update(@RequestBody CateDTO cateDTO) {
    Cate savedEntity = cateService.update(cateDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제, http://localhost:9100/cate/9
   * 
   * @param issueDTO
   * @return
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = cateService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build(); // 성공적으로 삭제시 200
    } else {
      return ResponseEntity.notFound().build(); // 404 에러 발생
    }

  }
  
  /**
   * 화면 상단 2단 메뉴 출력, http://localhost:9100/cate/menu
   * @return
   */
  @GetMapping(path="/menu")
  public Map<String, Object> menu() {
    return this.cateService.getGrpName();
  }
  
  /* =========================
  🔍 카테고리 검색 + 페이징
  http://localhost:9100/cate/search
========================= */
@GetMapping("/search")
public ResponseEntity<Map<String, Object>> searchCate(
   @RequestParam(name = "keyword", defaultValue = "") String keyword,
   @RequestParam(name = "page", defaultValue = "0") int page,
   @RequestParam(name = "size", defaultValue = "10") int size
) {

 Page<Cate> result = cateService.searchCate(keyword, page, size);

 Map<String, Object> map = new HashMap<>();
 map.put("cates", result.getContent());        // 현재 페이지 데이터
 map.put("currentPage", result.getNumber());
 map.put("totalItems", result.getTotalElements());
 map.put("totalPages", result.getTotalPages());

 return ResponseEntity.ok(map);
}
  
}









