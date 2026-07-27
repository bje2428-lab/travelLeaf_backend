package dev.jpa.cate;

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
public class Cate {
  /**
   * 식별자, sequence 자동 생성됨.
   * 
   * @Id: Primary Key
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cate_seq")
  @SequenceGenerator(name = "cate_seq", sequenceName = "CATE_SEQ", allocationSize = 1)
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
  
  public Cate() {
    
  }

  public Cate(long cateno, String grp, String name, int cnt, int seqno, String visible, String rdate) {
    this.cateno = cateno;
    this.grp = grp;
    this.name = name;
    this.cnt = cnt;
    this.seqno = seqno;
    this.visible = visible;
    this.rdate = rdate;
  }
  
  
}








