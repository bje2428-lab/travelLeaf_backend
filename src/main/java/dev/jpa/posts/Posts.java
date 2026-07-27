package dev.jpa.posts;

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
public class Posts {

    /** 게시글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "POSTS_SEQ")
    @SequenceGenerator(name = "POSTS_SEQ", sequenceName = "POSTS_SEQ", allocationSize = 1)
    private long postId;

    /** 작성자 ID (FK → USER_TB) */
    private String userId;

    /** 카테고리 번호 (FK → CATE) */
    private long cateno;

    /** 제목 */
    private String title = "";

    /** 내용 */
    private String content = "";

    /** 추천수 */
    private int recom = 0;

    /** 조회수 */
    private int cnt = 0;

    /** 댓글수 */
    private int replycnt = 0;
    
    /** 즐겨찾기 수 */
    private int favoriteCnt = 0;

    /** 패스워드 */
    private String password = "";

    /** 검색어 */
    private String word = "";

    /** 등록일 */
    private String rdate = "";

    /** 지도 */
    private String map = "";

    /** Youtube */
    private String youtube = "";

    /** mp4 */
    private String mp4 = "";

    /** 출력 모드 (Y/N) */
    private String visible = "Y";

    // 파일 업로드 관련
    private String file1 = "";        // 메인 이미지
    private String file1saved = "";   // 실제 저장된 메인 이미지
    private String thumb1 = "";       // preview image
    private long size1 = 0;           // 이미지 크기

    // 쇼핑몰 상품 관련
    private int price = 0;       // 정가
    private int dc = 0;          // 할인률
    private int saleprice = 0;   // 판매가
    private int point = 0;       // 포인트
    private int salecnt = 0;     // 재고 수량

    public Posts() {}

    /**
     * 게시글 등록 (기본)
     */
    public Posts(String userId, long cateno, String title, String content, String password, String word, String rdate) {
        this.userId = userId;
        this.cateno = cateno;
        this.title = title;
        this.content = content;
        this.password = password;
        this.word = word;
        this.rdate = rdate;
    }

    /**
     * 게시글 등록 (파일 포함)
     */
    public Posts(String userId, long cateno, String title, String content, String password, String word, String rdate,
                 String file1, String file1saved, String thumb1, long size1) {
        this.userId = userId;
        this.cateno = cateno;
        this.title = title;
        this.content = content;
        this.password = password;
        this.word = word;
        this.rdate = rdate;
        this.file1 = file1;
        this.file1saved = file1saved;
        this.thumb1 = thumb1;
        this.size1 = size1;
    }

    /**
     * 게시글 수정용 DTO 변환
     */
    public PostsDTO toDTO() {
        return new PostsDTO(
                this.postId,
                this.userId,
                this.cateno,
                this.title,
                this.content,
                this.cnt,
                this.password,
                this.word,
                this.rdate,
                this.youtube,
                this.map,
                this.file1,
                this.file1saved,
                this.thumb1,
                this.size1
        );
    }
}
