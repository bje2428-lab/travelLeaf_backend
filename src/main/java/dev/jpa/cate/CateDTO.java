package dev.jpa.cate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString
public class CateDTO {
  private long cateno;
  
  /** 대분류명 */
  private String grp;
  
  /** 중분류명 */
  private String name;
  
  /** 관련 자료수 */
  private int cnt;
  
  /** 출력 순서 */
  private int seqno;
  
  /** 숨기기 여부 */
  private String visible;
  
  /** 게시판 생성일 */
  private String rdate;
  
  /**
   * DTO(Java) -> Entity(DBMS)
   * @return
   */
  public Cate toEntity() {
    return new Cate(cateno, grp, name, cnt, seqno, visible, rdate);
  }
  
}


