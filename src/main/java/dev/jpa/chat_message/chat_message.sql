-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE CHAT_MESSAGE CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE CHAT_MESSAGE (
    MSG_ID        NUMBER(20)      NOT NULL,        -- 메시지 고유 ID
    ROOM_ID       NUMBER(20)      NOT NULL,        -- 채팅방 ID
    SENDER_TYPE   VARCHAR2(20)    NOT NULL,        -- USER / HOTEL
    SENDER_NO     NUMBER(20),                      -- USER면 USER_NO, HOTEL이면 null
    CONTENT       VARCHAR2(2000)  NOT NULL,        -- 메시지 내용
    SENT_AT       DATE            DEFAULT SYSDATE,
    PRIMARY KEY (MSG_ID),
    FOREIGN KEY (ROOM_ID) REFERENCES CHAT_ROOM(ROOM_ID),
    FOREIGN KEY (SENDER_NO) REFERENCES USER_TB(USER_NO),
    
    CONSTRAINT CK_CHAT_SENDER_TYPE CHECK (SENDER_TYPE IN ('USER', 'HOTEL'))
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE CHAT_MESSAGE IS '채팅 메시지 내역';
COMMENT ON COLUMN CHAT_MESSAGE.MSG_ID IS '메시지 고유 ID';
COMMENT ON COLUMN CHAT_MESSAGE.ROOM_ID IS '채팅방 ID';
COMMENT ON COLUMN CHAT_MESSAGE.SENDER_TYPE IS '메시지 보낸 주체(USER/HOTEL)';
COMMENT ON COLUMN CHAT_MESSAGE.SENDER_NO IS '사용자 번호(호텔이면 null)';
COMMENT ON COLUMN CHAT_MESSAGE.CONTENT IS '메시지 내용';
COMMENT ON COLUMN CHAT_MESSAGE.SENT_AT IS '메시지 전송 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE CHAT_MESSAGE_SEQ;

CREATE SEQUENCE CHAT_MESSAGE_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;

-----------------------------------------------------------
-- INSERT (Dummy/Test)
-----------------------------------------------------------
-- 유저가 보낸 메시지
INSERT INTO CHAT_MESSAGE (MSG_ID, ROOM_ID, SENDER_TYPE, SENDER_NO, CONTENT, SENT_AT)
VALUES (CHAT_MESSAGE_SEQ.NEXTVAL, 1, 'USER', 17, '안녕하세요 문의드릴게요.', sysdate);

-- 호텔이 보낸 메시지
INSERT INTO CHAT_MESSAGE (MSG_ID, ROOM_ID, SENDER_TYPE, SENDER_NO, CONTENT, SENT_AT)
VALUES (CHAT_MESSAGE_SEQ.NEXTVAL, 1, 'HOTEL', NULL, '안녕하세요. 어떤 내용이신가요?', sysdate);

COMMIT;

-----------------------------------------------------------
-- SELECT (조회)
-----------------------------------------------------------
-- 전체 메시지 조회
SELECT MSG_ID, ROOM_ID, SENDER_TYPE, SENDER_NO, CONTENT, SENT_AT
FROM CHAT_MESSAGE
ORDER BY MSG_ID;

-- 특정 채팅방 메시지
SELECT *
FROM CHAT_MESSAGE
WHERE ROOM_ID = 1
ORDER BY SENT_AT ASC;

-- 특정 유저가 보낸 메시지
SELECT *
FROM CHAT_MESSAGE
WHERE SENDER_NO = 17;
