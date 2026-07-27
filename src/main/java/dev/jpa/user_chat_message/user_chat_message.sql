-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE USER_CHAT_MESSAGE CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE USER_CHAT_MESSAGE (
    MSG_ID        NUMBER(20)      NOT NULL,        -- 메시지 고유 ID
    ROOM_ID       NUMBER(20)      NOT NULL,        -- 채팅방 ID
    SENDER_NO     NUMBER(20)      NOT NULL,        -- 메시지 보낸 회원 번호
    CONTENT       VARCHAR2(2000)  NOT NULL,        -- 메시지 내용
    SENT_AT       DATE            DEFAULT SYSDATE, -- 전송 시간
    PRIMARY KEY (MSG_ID),
    FOREIGN KEY (ROOM_ID) REFERENCES CHAT_ROOM(ROOM_ID),
    FOREIGN KEY (SENDER_NO) REFERENCES USER_TB(USER_NO)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE USER_CHAT_MESSAGE IS '회원 간 채팅 메시지 내역';
COMMENT ON COLUMN USER_CHAT_MESSAGE.MSG_ID IS '메시지 고유 ID';
COMMENT ON COLUMN USER_CHAT_MESSAGE.ROOM_ID IS '회원 채팅방 ID';
COMMENT ON COLUMN USER_CHAT_MESSAGE.SENDER_NO IS '메시지 보낸 회원 번호';
COMMENT ON COLUMN USER_CHAT_MESSAGE.CONTENT IS '메시지 내용';
COMMENT ON COLUMN USER_CHAT_MESSAGE.SENT_AT IS '메시지 전송 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE USER_CHAT_MESSAGE_SEQ;

CREATE SEQUENCE USER_CHAT_MESSAGE_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
