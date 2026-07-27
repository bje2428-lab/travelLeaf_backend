package dev.jpa.posts;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PostsDTO {

    private List<String> tags;

    /** 게시글 번호 */
    private long postId;

    /** 작성자 회원 ID */
    private String userId;

    /** 카테고리 번호 */
    private long cateno;

    /** 제목 */
    private String title = "";

    /** 내용 */
    private String content = "";

    /** 조회수 */
    private int cnt = 0;

    /** 패스워드 */
    private String password = "";

    /** 검색어 */
    private String word = "";

    /** 등록일 */
    private String rdate = "";

    /** 지도 (카카오맵) */
    private String map = "";

    /** Youtube */
    private String youtube = "";

    /** mp4 */
    private String mp4 = "";

    /** 숨기기 여부 */
    private String visible;

    // ---------------- 파일 업로드 ----------------
    private MultipartFile file1MF = null;
    private String size1_label = "";
    private String file1 = "";
    private String file1saved = "";
    private String thumb1 = "";
    private long size1 = 0;

    // ---------------- 쇼핑몰 관련 ----------------
    private int price = 0;
    private int dc = 0;
    private int saleprice = 0;
    private int point = 0;
    private int salecnt = 0;

    /** 기본 생성자 */
    public PostsDTO() {}

    /**
     * 🔥 Posts 엔티티 → DTO 변환 생성자
     * (Page.map(PostsDTO::new) 용)
     */
    public PostsDTO(Posts post) {
        this.postId = post.getPostId();
        this.userId = post.getUserId();
        this.cateno = post.getCateno();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.cnt = post.getCnt();
        this.password = post.getPassword();
        this.word = post.getWord();
        this.rdate = post.getRdate();
        this.youtube = post.getYoutube();
        this.map = post.getMap();
        this.file1 = post.getFile1();
        this.file1saved = post.getFile1saved();
        this.thumb1 = post.getThumb1();
        this.size1 = post.getSize1();
        this.visible = post.getVisible();
    }

    /**
     * Posts 엔티티 → DTO (기존 생성자)
     */
    public PostsDTO(
        long postId,
        String userId,
        long cateno,
        String title,
        String content,
        int cnt,
        String password,
        String word,
        String rdate,
        String youtube,
        String map,
        String file1,
        String file1saved,
        String thumb1,
        long size1
    ) {
        this.postId = postId;
        this.userId = userId;
        this.cateno = cateno;
        this.title = title;
        this.content = content;
        this.cnt = cnt;
        this.password = password;
        this.word = word;
        this.rdate = rdate;
        this.youtube = youtube;
        this.map = map;
        this.file1 = file1;
        this.file1saved = file1saved;
        this.thumb1 = thumb1;
        this.size1 = size1;
    }

    /**
     * DTO → 엔티티 (텍스트만)
     * 🔥 youtube / map 반드시 포함
     */
    public Posts toEntityOnlyText() {
        Posts post = new Posts(userId, cateno, title, content, password, word, rdate);
        post.setYoutube(this.youtube);
        post.setMap(this.map);
        return post;
    }

    /**
     * DTO → 엔티티 (파일 포함)
     * 🔥 youtube / map 반드시 포함
     */
    public Posts toEntityWithFile() {
        Posts post = new Posts(
            userId,
            cateno,
            title,
            content,
            password,
            word,
            rdate,
            file1,
            file1saved,
            thumb1,
            size1
        );
        post.setYoutube(this.youtube);
        post.setMap(this.map);
        return post;
    }
}
