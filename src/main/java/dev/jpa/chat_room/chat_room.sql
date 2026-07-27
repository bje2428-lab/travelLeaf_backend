-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE CHAT_ROOM CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE CHAT_ROOM (
    ROOM_ID        NUMBER(20)    NOT NULL,       -- 채팅방 고유 ID
    USER_NO        NUMBER(20)    NOT NULL,       -- 회원 PK
    HOTEL_EXT_ID   VARCHAR2(100) NOT NULL,       -- 외부 호텔 ID(place_id)
    HOTEL_NAME     VARCHAR2(100)  NOT NULL,
    CREATED_AT     DATE          DEFAULT SYSDATE,
    PRIMARY KEY (ROOM_ID),
    FOREIGN KEY (USER_NO) REFERENCES USER_TB(USER_NO),
    
    CONSTRAINT UK_CHAT_ROOM UNIQUE (USER_NO, HOTEL_EXT_ID)
);


-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE CHAT_ROOM IS '사용자-호텔 1:1 채팅방';
COMMENT ON COLUMN CHAT_ROOM.ROOM_ID IS '채팅방 고유 ID';
COMMENT ON COLUMN CHAT_ROOM.USER_NO IS '사용자 번호';
COMMENT ON COLUMN CHAT_ROOM.HOTEL_EXT_ID IS '카카오 외부 호텔 ID';
COMMENT ON COLUMN CHAT_ROOM.HOTEL_NAME IS '호텔 이름';
COMMENT ON COLUMN CHAT_ROOM.CREATED_AT IS '채팅방 생성 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE CHAT_ROOM_SEQ;

CREATE SEQUENCE CHAT_ROOM_SEQ 
  START WITH 1
  INCREMENT BY 1;

-----------------------------------------------------------
-- INSERT (Dummy/Test)
-----------------------------------------------------------
INSERT INTO CHAT_ROOM (ROOM_ID, USER_NO, HOTEL_EXT_ID, HOTEL_NAME)
VALUES (CHAT_ROOM_SEQ.NEXTVAL, 25, '1', '가나다');  -- 카카오 place_id 예시

INSERT INTO CHAT_ROOM (ROOM_ID, USER_NO, HOTEL_EXT_ID, HOTEL_NAME)
VALUES (CHAT_ROOM_SEQ.NEXTVAL, 25, '2', '테스트');  -- 카카오 place_id 예시

COMMIT;

-----------------------------------------------------------
-- SELECT (조회)
-----------------------------------------------------------
-- 전체 조회
SELECT ROOM_ID, USER_NO, HOTEL_EXT_ID, CREATED_AT
FROM CHAT_ROOM
ORDER BY ROOM_ID;

-- 특정 유저의 채팅방
SELECT *
FROM CHAT_ROOM
WHERE USER_NO = 17;

-- 특정 호텔 관련 채팅방
SELECT *
FROM CHAT_ROOM
WHERE HOTEL_EXT_ID = '1';
