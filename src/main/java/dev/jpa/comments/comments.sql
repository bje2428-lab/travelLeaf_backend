
-- ============================================
-- 1. 기존 테이블 삭제 (존재할 경우)
-- ============================================
DROP TABLE COMMENTS CASCADE CONSTRAINTS;
DROP SEQUENCE comments_seq;

------------------------------------------------------------
-- 2. COMMENTS 테이블 생성
------------------------------------------------------------
CREATE TABLE COMMENTS (
    comment_id          NUMBER PRIMARY KEY,          -- 댓글 ID
    post_id             NUMBER NOT NULL,             -- 게시글(FK → POSTS.post_id)
    user_id             VARCHAR2(50) NOT NULL,       -- 작성자(FK → USER_TB.user_id)
    parent_comment_id   NUMBER NULL,                 -- 부모 댓글(FK → COMMENTS.comment_id)
    content             VARCHAR2(2000) NOT NULL,     -- 내용
    image_url           VARCHAR2(500),               -- 이미지 URL
    is_deleted          CHAR(1) DEFAULT 'N',         -- 삭제 여부('Y', 'N')
    created_at          DATE DEFAULT SYSDATE,        -- 작성일
    updated_at          DATE                         -- 수정일
);

------------------------------------------------------------
-- 3. 컬럼 설명
------------------------------------------------------------
COMMENT ON TABLE COMMENTS IS '게시글 댓글';
COMMENT ON COLUMN COMMENTS.comment_id IS '댓글 ID';
COMMENT ON COLUMN COMMENTS.post_id IS '대상 게시글 ID(FK)';
COMMENT ON COLUMN COMMENTS.user_id IS '작성자 ID(FK)';
COMMENT ON COLUMN COMMENTS.parent_comment_id IS '부모 댓글 ID(FK)';
COMMENT ON COLUMN COMMENTS.content IS '댓글 내용';
COMMENT ON COLUMN COMMENTS.image_url IS '이미지 URL';
COMMENT ON COLUMN COMMENTS.is_deleted IS '삭제 여부(Y/N)';
COMMENT ON COLUMN COMMENTS.created_at IS '작성일';
COMMENT ON COLUMN COMMENTS.updated_at IS '수정일';

------------------------------------------------------------
-- 4. 외래키 설정
------------------------------------------------------------

-- USER_TB 연결
ALTER TABLE COMMENTS
ADD CONSTRAINT fk_comments_user
FOREIGN KEY (user_id)
REFERENCES USER_TB(user_id);

-- POSTS 연결
ALTER TABLE COMMENTS
ADD CONSTRAINT fk_comments_post
FOREIGN KEY (post_id)
REFERENCES POSTS(post_id);

-- 부모 댓글(Self Join)
ALTER TABLE COMMENTS
ADD CONSTRAINT fk_comments_parent
FOREIGN KEY (parent_comment_id)
REFERENCES COMMENTS(comment_id);

------------------------------------------------------------
-- 5. 시퀀스 생성
------------------------------------------------------------
CREATE SEQUENCE COMMENTS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

------------------------------------------------------------
-- 6. CRUD SQL 샘플
------------------------------------------------------------

-----------------------------
-- [댓글 작성]
-----------------------------
INSERT INTO COMMENTS (
    comment_id, post_id, user_id, parent_comment_id, content, image_url,
    created_at, updated_at
) VALUES (
    COMMENTS_SEQ.NEXTVAL,
    :post_id,
    :user_id,
    NULL,
    :content,
    :image_url,
    SYSDATE,
    NULL
);

-----------------------------
-- [대댓글 작성]
-----------------------------
INSERT INTO COMMENTS (
    comment_id, post_id, user_id, parent_comment_id, content, image_url,
    created_at, updated_at
) VALUES (
    COMMENTS_SEQ.NEXTVAL,
    :post_id,
    :user_id,
    :parent_comment_id,
    :content,
    :image_url,
    SYSDATE,
    NULL
);

-----------------------------
-- [댓글 목록 조회 - 계층형]
-----------------------------
SELECT
    c.comment_id,
    c.post_id,
    c.user_id,
    u.nickname AS author_nickname,
    c.parent_comment_id,
    CASE WHEN c.is_deleted = 'Y'
         THEN '삭제된 댓글입니다.'
         ELSE c.content
    END AS display_content,
    c.image_url,
    c.is_deleted,
    c.created_at,
    c.updated_at,
    LEVEL AS depth
FROM COMMENTS c
JOIN USER_TB u ON c.user_id = u.user_id
WHERE c.post_id = :post_id
START WITH c.parent_comment_id IS NULL
CONNECT BY PRIOR c.comment_id = c.parent_comment_id
ORDER SIBLINGS BY c.created_at ASC;

-----------------------------
-- [댓글 수정]
-----------------------------
UPDATE COMMENTS
SET content = :content,
    image_url = :image_url,
    updated_at = SYSDATE
WHERE comment_id = :comment_id
  AND user_id = :user_id
  AND is_deleted = 'N';

-----------------------------
-- [댓글 soft delete - 사용자]
-----------------------------
UPDATE COMMENTS
SET is_deleted = 'Y',
    content = '삭제된 댓글입니다.',
    image_url = NULL,
    updated_at = SYSDATE
WHERE comment_id = :comment_id
  AND user_id = :user_id;

-----------------------------
-- [댓글 soft delete - 관리자]
-----------------------------
UPDATE COMMENTS
SET is_deleted = 'Y',
    content = '관리자에 의해 삭제된 댓글입니다.',
    image_url = NULL,
    updated_at = SYSDATE
WHERE comment_id = :comment_id;

-----------------------------
-- [댓글 완전 삭제 (관리자)]
-----------------------------
DELETE FROM COMMENTS
WHERE comment_id = :comment_id;

-----------------------------
-- [대댓글 개수 조회]
-----------------------------
SELECT COUNT(*)
FROM COMMENTS
WHERE parent_comment_id = :parent_comment_id
  AND is_deleted = 'N';
