package dev.jpa.cate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import dev.jpa.tool.Tool;

@Service
public class CateService {
  @Autowired
  CateRepository cateRepository;
  
  public CateService() {
    
  }
  
  /**
   * 등록
   * @param cateDTO
   * @return
   */
  public Cate save(CateDTO cateDTO) {
    cateDTO.setRdate(Tool.getDate());
    Cate cate = cateRepository.save(cateDTO.toEntity());
    
    return cate;
  }
  
  public List<Cate> findAllByOrderByGrpAscNameAsc() {
    List<Cate> list = cateRepository.findAllByOrderByGrpAscNameAsc();
    
    return list;
  }
  
  public List<Cate> findAllByOrderBySeqnoAsc() {
    List<Cate> list = cateRepository.findAllByOrderBySeqnoAsc();
    
    return list;
  }
  
  public Cate findById(long pk) {
    Optional<Cate> optional = cateRepository.findById(pk);
    
    if (optional.isPresent()) {
      Cate cate = optional.get();
      return cate;
    }
      return null;
  }
  
  /**
   * 조회 + 수정 처리
   * @param cateDTO
   * @return
   */
  public Cate update(CateDTO cateDTO) {
    Cate cate = cateRepository.findById(cateDTO.getCateno()).get();
    cate.setGrp(cateDTO.getGrp());
    cate.setName(cateDTO.getName());
    cate.setCnt(cateDTO.getCnt());
    cate.setSeqno(cateDTO.getSeqno());
    cate.setVisible(cateDTO.getVisible());
    
    Cate savedEntity = cateRepository.save(cate);
    
    return savedEntity;
    
  }

  /**
   * 조회 +  삭제 처리
   * @param pk
   * @return
   */
  public boolean deleteById(long pk) {
    Optional<Cate> optional = cateRepository.findById(pk);
    
    if (optional.isPresent()) {
      cateRepository.deleteById(pk);
      return true;
    }
      return false;
  }
  
  public Map<String, Object> getGrpName() {
    Map<String, Object> map = new HashMap<>(); // 데이터 전체 저장 map
    List<Map<String, Object>> main = null; // 대분류 배열
    Map<String, Object> main_object = null; // 대분류 배열의 객체
    List<Object> middle_list = null; // 중분류 배열
    
    // name(중분류) per grp(대분류)
    main = new ArrayList<>();
    for(Cate cate : this.cateRepository.getGrp()) {
      main_object = new HashMap<>();
      
      String grp = cate.getGrp(); // 대분류명
      main_object.put("grp", grp);
      
      middle_list = new ArrayList<>(); // 중분류 배열
      for(Cate item: this.cateRepository.getGrpName(grp)) { // 대분류명에 해당하는 중분류 산출
        // System.out.println(item.toString());
        middle_list.add(item);  // 중분류 배열에 중분류 객체 저장
      }
      main_object.put("item",  middle_list);
      
      main.add(main_object);
      
    }
    map.put("main", main);
    
    
    return map;
  }
  
  public Page<Cate> searchCate(String keyword, int page, int size) {

    PageRequest pageable =
        PageRequest.of(page, size, Sort.by("seqno").ascending());

    return cateRepository.searchCate(keyword, pageable);
  }
  
  
}






