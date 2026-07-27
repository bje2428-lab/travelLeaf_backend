----------------------------------------------------------------------------------
---- 친구 (FRIENDS)
---- 설명:
----  - FRIEND_ID: 관계 엔티티의 고유 식별자 (시퀀스 사용)
----  - REQUESTER_ID / RECEIVER_ID: USER_TB.USER_ID를 참조 (문자열 ID 사용)
----  - SW: 상태 (0: 대기, 1: 수락) — 필요 시 확장 가능 (거절/차단 등)
----  - FRIENDS는 관계 엔티티이므로 별도 NO 컬럼 없음
----------------------------------------------------------------------------------
--
---- (주의) 기존 테이블 삭제: 개발/테스트 환경에서만 실행하세요.
--DROP TABLE FRIENDS CASCADE CONSTRAINTS;
--
--commit;
--
--CREATE TABLE FRIENDS (
--    FRIEND_ID     NUMBER           PRIMARY KEY,
--    
--    REQUESTER_ID  VARCHAR2(50)      NOT NULL,
--    RECEIVER_ID   VARCHAR2(50)      NOT NULL,
--
--    -- 상태
--    -- 0: 요청중
--    -- 1: 수락
--    -- 2: 거절
--    -- 3: 차단
--    SW            NUMBER(1)         DEFAULT 0 NOT NULL,
--
--    -- 날짜
--    REQ_DATE      DATE              DEFAULT SYSDATE NOT NULL,
--    RES_DATE      DATE,
--
--    CONSTRAINT FK_FRIEND_REQ
--        FOREIGN KEY (REQUESTER_ID)
--        REFERENCES USER_TB (USER_ID),
--
--    CONSTRAINT FK_FRIEND_REC
--        FOREIGN KEY (RECEIVER_ID)
--        REFERENCES USER_TB (USER_ID),
--
--    -- 자기 자신에게 요청 방지
--    CONSTRAINT CK_FRIEND_SELF
--        CHECK (REQUESTER_ID <> RECEIVER_ID)
--);
--
--commit;
--
---- FRIENDS 시퀀스 삭제 (존재하면)
--DROP SEQUENCE FRIENDS_SEQ;
--
---- FRIENDS 시퀀스 생성 (FRIEND_ID에 사용)
--CREATE SEQUENCE FRIENDS_SEQ
--START WITH 1
--INCREMENT BY 1
--NOCACHE
--NOCYCLE;
--
--CREATE UNIQUE INDEX UIX_FRIEND_PAIR
--ON FRIENDS (
--    LEAST(REQUESTER_ID, RECEIVER_ID),
--    GREATEST(REQUESTER_ID, RECEIVER_ID)
--);
--
--CREATE INDEX IDX_FRIEND_REQ ON FRIENDS (REQUESTER_ID);
--CREATE INDEX IDX_FRIEND_REC ON FRIENDS (RECEIVER_ID);
--CREATE INDEX IDX_FRIEND_SW ON FRIENDS (SW);
--
--
--SELECT * FROM ALL_TABLES WHERE TABLE_NAME = 'FRIENDS';
--
---- -----------------------
---- CRUD 예시 (관계 생성/수락/삭제)
---- -----------------------
--
---- 친구 요청 등록 (FRIEND_ID는 시퀀스로 채움)
--INSERT INTO FRIENDS (
--    FRIEND_ID, SW, REQUESTER_ID, RECEIVER_ID
--) VALUES (
--    FRIENDS_SEQ.NEXTVAL, 0, 'user1', 'user2'
--);
--
---- 전체 친구 관계 조회
--SELECT * FROM FRIENDS;
--
---- 특정 회원 친구 조회 (요청자 또는 수신자 포함된 모든 행)
--SELECT * FROM FRIENDS
--WHERE REQUESTER_ID = 'user1' OR RECEIVER_ID = 'user1';
--
---- 친구 수락 (FRIEND_ID 기준)
--UPDATE FRIENDS SET SW = 1 WHERE FRIEND_ID = 1;
--
---- 친구 관계 삭제
--DELETE FROM FRIENDS WHERE FRIEND_ID = 1;



--------------------------------------------------------------------------------
-- 친구 (FRIENDS)
-- 설명:
--  - FRIEND_ID: 관계 엔티티의 고유 식별자 (시퀀스 사용)
--  - REQUESTER_ID / RECEIVER_ID: USER_TB.USER_ID를 참조 (문자열 ID 사용)
--  - REQUESTER_NO / RECEIVER_NO: USER_TB.USER_NO를 참조 (내부 번호, 채팅용)
--  - SW: 상태 (0: 대기, 1: 수락, 2: 거절, 3: 차단)
--------------------------------------------------------------------------------

-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE FRIENDS CASCADE CONSTRAINTS;

COMMIT;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE FRIENDS (
    FRIEND_ID     NUMBER           PRIMARY KEY,

    REQUESTER_ID  VARCHAR2(50)      NOT NULL,
    RECEIVER_ID   VARCHAR2(50)      NOT NULL,

    -- 🔥 채팅용 내부 회원 번호
    REQUESTER_NO  NUMBER            NOT NULL,
    RECEIVER_NO   NUMBER            NOT NULL,

    -- 상태
    -- 0: 요청중
    -- 1: 수락
    -- 2: 거절
    -- 3: 차단
    SW            NUMBER(1)         DEFAULT 0 NOT NULL,

    -- 날짜
    REQ_DATE      DATE              DEFAULT SYSDATE NOT NULL,
    RES_DATE      DATE,

    -------------------------------------------------------
    -- FK (USER_ID)
    -------------------------------------------------------
    CONSTRAINT FK_FRIEND_REQ
        FOREIGN KEY (REQUESTER_ID)
        REFERENCES USER_TB (USER_ID),

    CONSTRAINT FK_FRIEND_REC
        FOREIGN KEY (RECEIVER_ID)
        REFERENCES USER_TB (USER_ID),

    -------------------------------------------------------
    -- 🔥 FK (USER_NO) : 채팅용
    -------------------------------------------------------
    CONSTRAINT FK_FRIEND_REQ_NO
        FOREIGN KEY (REQUESTER_NO)
        REFERENCES USER_TB (USER_NO),

    CONSTRAINT FK_FRIEND_REC_NO
        FOREIGN KEY (RECEIVER_NO)
        REFERENCES USER_TB (USER_NO),

    -- 자기 자신에게 요청 방지
    CONSTRAINT CK_FRIEND_SELF
        CHECK (REQUESTER_ID <> RECEIVER_ID),

    CONSTRAINT CK_FRIEND_SELF_NO
        CHECK (REQUESTER_NO <> RECEIVER_NO)
);

COMMIT;

-----------------------------------------------------------
-- FRIENDS 시퀀스 삭제 (존재하면)
-----------------------------------------------------------
DROP SEQUENCE FRIENDS_SEQ;

-----------------------------------------------------------
-- FRIENDS 시퀀스 생성 (FRIEND_ID에 사용)
-----------------------------------------------------------
CREATE SEQUENCE FRIENDS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-----------------------------------------------------------
-- INDEX
-----------------------------------------------------------
CREATE UNIQUE INDEX UIX_FRIEND_PAIR
ON FRIENDS (
    LEAST(REQUESTER_ID, RECEIVER_ID),
    GREATEST(REQUESTER_ID, RECEIVER_ID)
);

CREATE INDEX IDX_FRIEND_REQ     ON FRIENDS (REQUESTER_ID);
CREATE INDEX IDX_FRIEND_REC     ON FRIENDS (RECEIVER_ID);
CREATE INDEX IDX_FRIEND_REQ_NO  ON FRIENDS (REQUESTER_NO);
CREATE INDEX IDX_FRIEND_REC_NO  ON FRIENDS (RECEIVER_NO);
CREATE INDEX IDX_FRIEND_SW      ON FRIENDS (SW);

-----------------------------------------------------------
-- 확인
-----------------------------------------------------------
SELECT * FROM ALL_TABLES WHERE TABLE_NAME = 'FRIENDS';

-----------------------------------------------------------
-- CRUD 예시 (관계 생성/수락/삭제)
-----------------------------------------------------------

-- 친구 요청 등록 (FRIEND_ID는 시퀀스로 채움)
-- ※ REQUESTER_NO / RECEIVER_NO는 USER_TB.USER_NO 값
INSERT INTO FRIENDS (
    FRIEND_ID,
    SW,
    REQUESTER_ID,
    RECEIVER_ID,
    REQUESTER_NO,
    RECEIVER_NO
) VALUES (
    FRIENDS_SEQ.NEXTVAL,
    0,
    'user1',
    'user2',
    1,
    2
);

-- 전체 친구 관계 조회
SELECT * FROM FRIENDS;

-- 특정 회원 친구 조회 (요청자 또는 수신자 포함된 모든 행)
SELECT * FROM FRIENDS
WHERE REQUESTER_ID = 'user1'
   OR RECEIVER_ID  = 'user1';

-- 친구 수락 (FRIEND_ID 기준)
UPDATE FRIENDS
SET SW = 1,
    RES_DATE = SYSDATE
WHERE FRIEND_ID = 1;

-- 친구 관계 삭제
DELETE FROM FRIENDS
WHERE FRIEND_ID = 1;
