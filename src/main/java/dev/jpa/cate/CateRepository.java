package dev.jpa.cate;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CateRepository extends JpaRepository<Cate, Long>{
  // 1) 등록
  
  // 2) 목록(Grp: ASC, Name: ASC)
  List<Cate> findAllByOrderByGrpAscNameAsc();
  List<Cate> findAllByOrderBySeqnoAsc();
  
  // 3) 조회
    
  // 4) 수정
  
  // 5) 삭제
  
  // 6) 공개된 대분류만 출력(*)
  @Query(value=""
      + "SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate "
      + "WHERE name='--' AND visible='Y' ORDER BY seqno ASC", nativeQuery = true)
  public List<Cate> getGrp();

  // 7) 개발 그룹의 중분류 출력(*)
  @Query(value=""
      + "SELECT cateno, grp, name, cnt, seqno, visible, rdate "
      + "FROM cate "
      + "WHERE grp=:grp AND name != '--' AND visible = 'Y' "
      + "ORDER BY seqno ASC", nativeQuery = true)
   public List<Cate> getGrpName(@Param("grp") String grp);
  
            /* =========================
            🔍 카테고리 검색 + 페이징
            grp 또는 name 기준
          ========================= */
          @Query("""
           SELECT c
           FROM Cate c
           WHERE
             (:keyword = '' OR
              c.grp LIKE %:keyword% OR
              c.name LIKE %:keyword%)
          """)
          Page<Cate> searchCate(
             @Param("keyword") String keyword,
             Pageable pageable
          );
            
  
}




