-- ============================================
-- 1. 기존 테이블 삭제 (존재할 경우) - 변경된 이름 반영
-- ============================================
DROP TABLE POSTS_TAGS CASCADE CONSTRAINTS;

-- ============================================
-- 2. POSTS_TAGS 테이블 생성 - 변경된 이름 반영
-- ============================================
CREATE TABLE POSTS_TAGS (
    post_id NUMBER NOT NULL, -- 게시글 ID (FK)
    tag_id  NUMBER NOT NULL, -- 태그 ID (FK)
    PRIMARY KEY(post_id, tag_id) -- 두 컬럼의 조합이 고유한 키
);

-- ============================================
-- 3. 테이블 및 컬럼 설명 - 변경된 이름 반영
-- ============================================
COMMENT ON TABLE POSTS_TAGS IS '게시글과 태그 연결 테이블';
COMMENT ON COLUMN POSTS_TAGS.post_id IS '게시글 ID';
COMMENT ON COLUMN POSTS_TAGS.tag_id IS '태그 ID';

-- ============================================
-- 4. FK 제약조건 (POSTS, TAGS 테이블이 있다고 가정) - 변경된 이름 반영
-- ============================================
ALTER TABLE POSTS_TAGS
ADD CONSTRAINT fk_pt_posts -- 제약조건 이름도 의미 있게 변경
FOREIGN KEY (post_id)
REFERENCES POSTS(post_id)
ON DELETE CASCADE; -- 게시글 삭제 시 해당 게시글의 태그 연결 정보도 함께 삭제

ALTER TABLE POSTS_TAGS
ADD CONSTRAINT fk_pt_tags -- 제약조건 이름도 의미 있게 변경
FOREIGN KEY (tag_id)
REFERENCES TAGS(tag_id)
ON DELETE CASCADE; -- 태그 삭제 시 해당 태그와 연결된 게시글 정보도 함께 삭제
                    -- (단, 이 옵션은 태그 삭제 시 게시글의 태그 정보가 사라지므로 신중해야 함.
                    -- 보통 태그는 ON DELETE RESTRICT (삭제 불가)하거나 SET NULL (FK 컬럼 NULL) 하는 경우도 있음)

-- ============================================
-- 5. POSTS_TAGS 기능별 SQL - 변경된 이름 반영
-- ============================================

-- [게시글에 태그 연결 (추가)]
-- 특정 게시글에 특정 태그를 연결합니다.
-- post_id와 tag_id의 조합이 PRIMARY KEY이므로 중복 연결은 불가능합니다.
INSERT INTO POSTS_TAGS (
    post_id, tag_id
) VALUES (
    :post_id, -- 연결할 게시글의 ID
    :tag_id   -- 연결할 태그의 ID
);

-- [게시글에서 태그 연결 해제 (삭제)]
-- 특정 게시글에서 특정 태그의 연결을 끊습니다.
DELETE FROM POSTS_TAGS
WHERE post_id = :post_id -- 태그를 해제할 게시글의 ID
  AND tag_id = :tag_id;   -- 해제할 태그의 ID


-- [특정 게시글에 달린 모든 태그 조회]
-- 게시글 상세 페이지 등에서 해당 게시글이 어떤 태그들을 가지고 있는지 보여줄 때 사용합니다.
SELECT t.tag_id, t.name
FROM TAGS t
JOIN POSTS_TAGS pt ON t.tag_id = pt.tag_id
WHERE pt.post_id = :post_id -- 조회하려는 게시글의 ID
ORDER BY t.name ASC; -- 태그 이름을 기준으로 정렬 (선택 사항)


-- [특정 태그가 달린 모든 게시글 조회]
-- 특정 태그를 클릭했을 때, 해당 태그가 달린 모든 게시글 목록을 보여줄 때 사용합니다.
SELECT
    p.post_id,
    p.title,
    p.content, -- 게시글 내용 등 필요한 정보 추가
    p.created_at,
    p.views
FROM POSTS p
JOIN POSTS_TAGS pt ON p.post_id = pt.post_id
WHERE pt.tag_id = :tag_id -- 조회하려는 태그의 ID
ORDER BY p.created_at DESC; -- 최신 게시글 순으로 정렬

-- (선택 사항) 태그 이름으로 검색하여 게시글 조회
-- 사용자에게 태그 ID 대신 태그 이름을 받아 검색하고 싶을 때 사용
SELECT
    p.post_id,
    p.title,
    p.content,
    p.created_at,
    p.views
FROM POSTS p
JOIN POSTS_TAGS pt ON p.post_id = pt.post_id
JOIN TAGS t ON pt.tag_id = t.tag_id
WHERE t.name = :tag_name -- 조회하려는 태그의 이름
ORDER BY p.created_at DESC;