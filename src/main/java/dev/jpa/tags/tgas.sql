-- ============================================
-- 1. 기존 테이블 삭제 (존재할 경우)
--    POST_TAGS가 TAGS를 참조하고 있을 수 있으니,
--    만약 POST_TAGS 테이블이 있다면 먼저 삭제하는 것이 안전합니다.
--    (여기서는 TAGS 테이블만 요청했으므로, TAGS만 삭제)
-- ============================================
DROP TABLE TAGS CASCADE CONSTRAINTS;
DROP SEQUENCE tags_seq;

-- ============================================
-- 2. TAGS 테이블 생성
-- ============================================
CREATE TABLE TAGS (
    tag_id       NUMBER PRIMARY KEY,          -- 태그 ID
    name         VARCHAR2(100) UNIQUE NOT NULL -- 태그 이름 (고유해야 함)
);

-- ============================================
-- 3. 테이블 및 컬럼 설명
-- ============================================
COMMENT ON TABLE TAGS IS '태그 정보';
COMMENT ON COLUMN TAGS.tag_id IS '태그 ID';
COMMENT ON COLUMN TAGS.name IS '태그 이름 (고유)';

-- ============================================
-- 4. 시퀀스 생성 (자동 번호 생성)
-- ============================================
CREATE SEQUENCE tags_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


-- ============================================
-- 5. 태그 기능별 SQL
-- ============================================

-- [태그 등록 (생성)]
-- 새로운 태그를 추가합니다. name 컬럼은 UNIQUE이므로 중복된 이름은 입력할 수 없습니다.
INSERT INTO TAGS (
    tag_id, name
) VALUES (
    tags_seq.NEXTVAL,
    :tag_name -- 새로 등록할 태그 이름
);

-- [태그 목록 조회]
-- 존재하는 모든 태그를 이름 순으로 조회합니다.
SELECT tag_id, name
FROM TAGS
ORDER BY name ASC;

-- [태그 검색]
-- 특정 키워드가 포함된 태그를 검색합니다. (예: '여행' 태그 검색 시 '국내여행', '해외여행' 등)
SELECT tag_id, name
FROM TAGS
WHERE name LIKE '%' || :search_keyword || '%'
ORDER BY name ASC;

-- [태그 수정]
-- 특정 태그 ID에 해당하는 태그의 이름을 변경합니다. (태그 이름은 고유해야 함)
UPDATE TAGS
SET
    name = :new_tag_name, -- 변경할 새 태그 이름
    -- updated_at 컬럼은 없지만, 혹시 나중에 추가된다면 SYSDATE로 업데이트 가능
    -- updated_at = SYSDATE
WHERE tag_id = :tag_id;

-- [태그 삭제]
-- 특정 태그 ID에 해당하는 태그를 삭제합니다.
-- 만약 이 태그가 다른 테이블(예: POST_TAGS)에서 참조되고 있다면,
-- 해당 참조 제약조건(FK) 설정에 따라 삭제가 실패하거나,
-- POST_TAGS의 ON DELETE CASCADE 설정에 따라 연결된 데이터가 함께 삭제될 수 있습니다.
-- 삭제 전 참조 관계를 반드시 확인하고 신중하게 실행해야 합니다.
DELETE FROM TAGS
WHERE tag_id = :tag_id;