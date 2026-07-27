------------------------------------------------------------
-- COMMENT_REACTIONS 테이블 삭제
------------------------------------------------------------
DROP TABLE COMMENT_REACTIONS CASCADE CONSTRAINTS;
DROP SEQUENCE COMMENT_REACTIONS_SEQ;

------------------------------------------------------------
-- COMMENT_REACTIONS 테이블 생성
------------------------------------------------------------
CREATE TABLE COMMENT_REACTIONS (
    reaction_no      NUMBER PRIMARY KEY,            -- 반응 고유 번호(SEQ 기반 PK)
    reaction_id      VARCHAR2(100) UNIQUE NOT NULL, -- 사람이 생성하는 문자열 ID (예: 'CR-user1-10-like')
    user_id          VARCHAR2(50) NOT NULL,         -- 사용자(FK)
    comment_id       NUMBER NOT NULL,               -- 댓글 ID (FK → COMMENTS.comment_id)
    type             VARCHAR2(20) NOT NULL,         -- like / favorite
    created_at       DATE DEFAULT SYSDATE           -- 생성일
);

------------------------------------------------------------
-- 테이블 및 컬럼 설명
------------------------------------------------------------
COMMENT ON TABLE COMMENT_REACTIONS IS '댓글 반응 테이블 (좋아요/즐겨찾기)';
COMMENT ON COLUMN COMMENT_REACTIONS.reaction_no IS '반응 고유 번호(PK)';
COMMENT ON COLUMN COMMENT_REACTIONS.reaction_id IS '사람 또는 서버가 생성하는 문자열 ID';
COMMENT ON COLUMN COMMENT_REACTIONS.user_id IS '사용자 ID(FK)';
COMMENT ON COLUMN COMMENT_REACTIONS.comment_id IS '댓글 ID(FK)';
COMMENT ON COLUMN COMMENT_REACTIONS.type IS '반응 종류(like/favorite)';
COMMENT ON COLUMN COMMENT_REACTIONS.created_at IS '생성일';

------------------------------------------------------------
-- 외래키 설정
------------------------------------------------------------
ALTER TABLE COMMENT_REACTIONS
ADD CONSTRAINT fk_comment_reactions_user
FOREIGN KEY (user_id)
REFERENCES USER_TB(user_id);

ALTER TABLE COMMENT_REACTIONS
ADD CONSTRAINT fk_comment_reactions_comment
FOREIGN KEY (comment_id)
REFERENCES COMMENTS(comment_id);

------------------------------------------------------------
-- 좋아요 + 즐겨찾기를 모두 허용하는 UNIQUE 설정
-- type이 다르면 좋아요와 즐겨찾기 둘 다 가능한 구조
------------------------------------------------------------
ALTER TABLE COMMENT_REACTIONS
ADD CONSTRAINT uq_comment_reactions UNIQUE (user_id, comment_id, type);

------------------------------------------------------------
-- COMMENT_REACTIONS 시퀀스 생성
------------------------------------------------------------
CREATE SEQUENCE COMMENT_REACTIONS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


------------------------------------------------------------
-- COMMENT_REACTIONS CRUD 예시
------------------------------------------------------------

-----------------------------
-- [댓글 반응 생성]
-----------------------------
INSERT INTO COMMENT_REACTIONS (
    reaction_no, reaction_id, user_id, comment_id, type, created_at
) VALUES (
    COMMENT_REACTIONS_SEQ.NEXTVAL,
    :reaction_id,    -- 예: CR-user1-10-like
    :user_id,
    :comment_id,
    :type,           -- like OR favorite
    SYSDATE
);

-----------------------------
-- [실행 예시]
-----------------------------
INSERT INTO COMMENT_REACTIONS (
    reaction_no, reaction_id, user_id, comment_id, type, created_at
) VALUES (
    COMMENT_REACTIONS_SEQ.NEXTVAL,
    'CR-user1-10-like',
    'user1',
    10,          -- 반드시 COMMENTS 테이블에 comment_id = 10 있어야 함
    'like',
    SYSDATE
);

INSERT INTO COMMENT_REACTIONS (
    reaction_no, reaction_id, user_id, comment_id, type, created_at
) VALUES (
    COMMENT_REACTIONS_SEQ.NEXTVAL,
    'CR-user1-10-favorite',
    'user1',
    10,
    'favorite',
    SYSDATE
);

-----------------------------
-- [댓글 반응 전체 조회]
-----------------------------
SELECT *
FROM COMMENT_REACTIONS;

-----------------------------
-- [특정 반응 조회]
-----------------------------
SELECT *
FROM COMMENT_REACTIONS
WHERE reaction_id = :reaction_id;

-----------------------------
-- [반응 종류 수정]
-- like → favorite 가능
-- 단, UNIQUE(user_id, comment_id, type) 위반 주의
-----------------------------
UPDATE COMMENT_REACTIONS
SET type = :type
WHERE reaction_id = :reaction_id;

-----------------------------
-- [반응 삭제]
-----------------------------
DELETE FROM COMMENT_REACTIONS
WHERE reaction_no = :reaction_no;

