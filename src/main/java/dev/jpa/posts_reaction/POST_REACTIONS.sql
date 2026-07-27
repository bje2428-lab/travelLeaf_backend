------------------------------------------------------------
-- POST_REACTIONS 테이블 삭제
------------------------------------------------------------
DROP TABLE POST_REACTIONS CASCADE CONSTRAINTS;
DROP SEQUENCE POST_REACTIONS_SEQ;

------------------------------------------------------------
-- POST_REACTIONS 테이블 생성
------------------------------------------------------------
CREATE TABLE POST_REACTIONS (
    reaction_no      NUMBER PRIMARY KEY,            -- 반응 고유 번호(SEQ 기반 PK)
    reaction_id      VARCHAR2(100) UNIQUE NOT NULL, -- 사람이 생성하는 문자열 ID (예: 'PR-user1-1-like')
    user_id          VARCHAR2(50) NOT NULL,         -- 사용자(FK)
    post_id          NUMBER NOT NULL,               -- 게시글 ID (FK → POSTS.post_id)
    type             VARCHAR2(20) NOT NULL,         -- like / favorite
    created_at       DATE DEFAULT SYSDATE           -- 생성일
);

------------------------------------------------------------
-- 테이블 및 컬럼 설명
------------------------------------------------------------
COMMENT ON TABLE POST_REACTIONS IS '게시글 반응 테이블(좋아요/즐겨찾기)';
COMMENT ON COLUMN POST_REACTIONS.reaction_no IS '반응 고유 번호(PK)';
COMMENT ON COLUMN POST_REACTIONS.reaction_id IS '사람 또는 서버가 생성하는 문자열 ID';
COMMENT ON COLUMN POST_REACTIONS.user_id IS '사용자 ID(FK)';
COMMENT ON COLUMN POST_REACTIONS.post_id IS '게시글 ID(FK)';
COMMENT ON COLUMN POST_REACTIONS.type IS '반응 종류(like/favorite)';
COMMENT ON COLUMN POST_REACTIONS.created_at IS '생성일';

------------------------------------------------------------
-- 외래키 설정
------------------------------------------------------------
ALTER TABLE POST_REACTIONS
ADD CONSTRAINT fk_post_reactions_user
FOREIGN KEY (user_id)
REFERENCES USER_TB(user_id);

ALTER TABLE POST_REACTIONS
ADD CONSTRAINT fk_post_reactions_post
FOREIGN KEY (post_id)
REFERENCES POSTS(post_id);

------------------------------------------------------------
-- 좋아요 + 즐겨찾기를 동시에 허용하기 위한 UNIQUE 설정
-- 사용자 + 게시글 + 타입 조합은 중복되면 안 됨
------------------------------------------------------------
ALTER TABLE POST_REACTIONS
ADD CONSTRAINT uq_post_reactions UNIQUE (user_id, post_id, type);

------------------------------------------------------------
-- POST_REACTIONS 시퀀스 생성
------------------------------------------------------------
CREATE SEQUENCE POST_REACTIONS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


------------------------------------------------------------
-- POST_REACTIONS CRUD 예시
------------------------------------------------------------

-----------------------------
-- [게시글 반응 생성]
-----------------------------
INSERT INTO POST_REACTIONS (
    reaction_no, reaction_id, user_id, post_id, type, created_at
) VALUES (
    POST_REACTIONS_SEQ.NEXTVAL,
    :reaction_id,   -- 예: PR-user1-1-like
    :user_id,
    :post_id,
    :type,          -- like OR favorite
    SYSDATE
);

-----------------------------
-- [예시 실행]
-----------------------------
INSERT INTO POST_REACTIONS (
    reaction_no, reaction_id, user_id, post_id, type, created_at
) VALUES (
    POST_REACTIONS_SEQ.NEXTVAL,
    'PR-user1-1-like',
    'user1',
    1,
    'like',
    SYSDATE
);

INSERT INTO POST_REACTIONS (
    reaction_no, reaction_id, user_id, post_id, type, created_at
) VALUES (
    POST_REACTIONS_SEQ.NEXTVAL,
    'PR-user1-1-favorite',
    'user1',
    1,
    'favorite',
    SYSDATE
);

-----------------------------
-- [게시글 반응 전체 조회]
-----------------------------
SELECT *
FROM POST_REACTIONS;

-----------------------------
-- [특정 반응 조회]
-----------------------------
SELECT *
FROM POST_REACTIONS
WHERE reaction_id = :reaction_id;

-----------------------------
-- [반응 수정 - type 변경]
-- 좋아요 → 즐겨찾기 같은 변경 가능(단, UNIQUE 위반 주의)
-----------------------------
UPDATE POST_REACTIONS
SET type = :type
WHERE reaction_id = :reaction_id;

-----------------------------
-- [반응 삭제]
-----------------------------
DELETE FROM POST_REACTIONS
WHERE reaction_no = :reaction_no;

commit;