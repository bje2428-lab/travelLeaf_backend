-- dev.jpa.resort.contents.posts.sql

-- ============================
-- 기존 POSTS 테이블 삭제
-- ============================
DROP TABLE POSTS CASCADE CONSTRAINTS;
DROP TABLE POSTS;

-- ============================
-- POSTS 테이블 생성
-- ============================
CREATE TABLE POSTS (
    post_id        NUMBER(10)         NOT NULL,       -- 게시글 PK
    user_id        VARCHAR2(50)       NOT NULL,       -- 작성자(FK → USER_TB)
    cateno         NUMBER(10)         NOT NULL,       -- 카테고리(FK → CATE)
    title          VARCHAR2(200)      NOT NULL,       -- 제목
    content        CLOB               NOT NULL,       -- 내용

    recom          NUMBER(7) DEFAULT 0 NOT NULL,      -- ❤️ 좋아요 수
    favorite_cnt   NUMBER(7) DEFAULT 0 NOT NULL,      -- ⭐ 즐겨찾기 수

    cnt            NUMBER(7) DEFAULT 0 NOT NULL,      -- 조회수
    replycnt       NUMBER(7) DEFAULT 0 NOT NULL,      -- 댓글수
    password       VARCHAR2(100)      NOT NULL,
    word           VARCHAR2(200)      NULL,
    rdate          VARCHAR2(255)      NOT NULL,

    file1          VARCHAR2(100)      NULL,
    file1saved     VARCHAR2(100)      NULL,
    thumb1         VARCHAR2(100)      NULL,
    size1          NUMBER(10) DEFAULT 0 NULL,

    price          NUMBER(10) DEFAULT 0 NULL,
    dc             NUMBER(10) DEFAULT 0 NULL,
    saleprice      NUMBER(10) DEFAULT 0 NULL,
    point          NUMBER(10) DEFAULT 0 NULL,
    salecnt        NUMBER(10) DEFAULT 0 NULL,

    map            VARCHAR2(1000)     NULL,
    youtube        VARCHAR2(1000)     NULL,
    mp4            VARCHAR2(100)      NULL,

    visible        CHAR(1)            DEFAULT 'Y' NOT NULL,

    PRIMARY KEY (post_id)
);

-- ============================
-- FK
-- ============================
ALTER TABLE POSTS ADD CONSTRAINT fk_posts_user
    FOREIGN KEY (user_id) REFERENCES USER_TB(USER_ID);

ALTER TABLE POSTS ADD CONSTRAINT fk_posts_cate
    FOREIGN KEY (cateno) REFERENCES CATE(CATENO);

-- ============================
-- COMMENT
-- ============================
COMMENT ON TABLE POSTS IS '게시글 테이블';
COMMENT ON COLUMN POSTS.post_id IS '게시글 번호';
COMMENT ON COLUMN POSTS.user_id IS '작성자 ID';
COMMENT ON COLUMN POSTS.cateno IS '카테고리 번호';
COMMENT ON COLUMN POSTS.title IS '제목';
COMMENT ON COLUMN POSTS.content IS '내용';

COMMENT ON COLUMN POSTS.recom IS '좋아요 수';
COMMENT ON COLUMN POSTS.favorite_cnt IS '즐겨찾기 수';

COMMENT ON COLUMN POSTS.cnt IS '조회수';
COMMENT ON COLUMN POSTS.replycnt IS '댓글수';
COMMENT ON COLUMN POSTS.password IS '패스워드';
COMMENT ON COLUMN POSTS.word IS '검색어';
COMMENT ON COLUMN POSTS.rdate IS '등록일';

COMMENT ON COLUMN POSTS.file1 IS '메인 이미지';
COMMENT ON COLUMN POSTS.file1saved IS '실제 저장된 메인 이미지';
COMMENT ON COLUMN POSTS.thumb1 IS '메인 이미지 Preview';
COMMENT ON COLUMN POSTS.size1 IS '메인 이미지 크기';

COMMENT ON COLUMN POSTS.map IS '지도';
COMMENT ON COLUMN POSTS.youtube IS 'Youtube 영상';
COMMENT ON COLUMN POSTS.mp4 IS '영상';
COMMENT ON COLUMN POSTS.visible IS '출력 모드';


-- ============================
-- 시퀀스
-- ============================
DROP SEQUENCE posts_seq;

CREATE SEQUENCE posts_seq
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 2
  NOCYCLE;

COMMIT;


-- ============================
-- 테스트 데이터
-- ============================
INSERT INTO POSTS(
    post_id, user_id, cateno, title, content,
    recom, favorite_cnt, cnt, replycnt,
    password, word, rdate,
    file1, file1saved, thumb1, size1, visible
)
VALUES(
    posts_seq.nextval,
    'user1',
    12,
    '대행사',
    '흙수저와 금수저의 성공 스토리',
    0,
    0,
    0,
    0,
    '123',
    '드라마,K드라마,넷플릭스',
    TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'),
    'space.jpg','space_1.jpg','space_t.jpg',1000,'Y'
);

COMMIT;


-- ============================
-- 전체 목록 조회
-- ⭐ favorite_cnt 포함됨
-- ============================
SELECT post_id, user_id, cateno, title, content,
       recom, favorite_cnt, cnt, replycnt, password, word, rdate,
       file1, file1saved, thumb1, size1,
       map, youtube, mp4, visible
FROM POSTS
ORDER BY post_id DESC;


-- ============================
-- 단일 조회
-- ============================
SELECT post_id, user_id, cateno, title, content,
       recom, favorite_cnt, cnt, replycnt, password, word, rdate,
       file1, file1saved, thumb1, size1,
       map, youtube, mp4, visible
FROM POSTS
WHERE post_id = 1;


-- ============================
-- ❤️ 좋아요 증가
-- ============================
UPDATE POSTS
SET recom = recom + 1
WHERE post_id = 1;

-- ============================
-- ⭐ 즐겨찾기 증가
-- ============================
UPDATE POSTS
SET favorite_cnt = favorite_cnt + 1
WHERE post_id = 1;

COMMIT;


-- ============================
-- 검색
-- ============================
SELECT post_id, user_id, cateno, title, content,
       recom, favorite_cnt, cnt, replycnt,
       rdate, file1, file1saved, thumb1, size1,
       map, youtube, mp4, visible
FROM POSTS
WHERE cateno = 12
AND (
    UPPER(title) LIKE '%' || UPPER('대행사') || '%'
 OR UPPER(content) LIKE '%' || UPPER('대행사') || '%'
 OR UPPER(word) LIKE '%' || UPPER('대행사') || '%'
)
ORDER BY post_id DESC;


-- ============================
-- 페이징
-- ⭐ favorite_cnt 포함
-- ============================
SELECT *
FROM (
    SELECT
        post_id, user_id, cateno, title, content,
        recom, favorite_cnt, cnt, replycnt,
        rdate, file1, file1saved, thumb1, size1,
        map, youtube, mp4, visible,
        ROW_NUMBER() OVER (ORDER BY rdate DESC) AS r
    FROM POSTS
    WHERE cateno = 12
)
WHERE r BETWEEN 1 AND 10;

COMMIT;
