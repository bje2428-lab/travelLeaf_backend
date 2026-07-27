-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE USER_CHAT_ROOM CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE USER_CHAT_ROOM (
    ROOM_ID        NUMBER(20)    NOT NULL,       -- 회원 채팅방 고유 ID
    USER_A_NO      NUMBER(20)    NOT NULL,       -- 회원 A
    USER_B_NO      NUMBER(20)    NOT NULL,       -- 회원 B
    LAST_READ_A_AT    DATE,                      -- A가 마지막으로 읽은 시간
    LAST_READ_B_AT    DATE,                      -- B가 마지막으로 읽은 시간
    CREATED_AT     DATE          DEFAULT SYSDATE,
    PRIMARY KEY (ROOM_ID),
    FOREIGN KEY (USER_A_NO) REFERENCES USER_TB(USER_NO),
    FOREIGN KEY (USER_B_NO) REFERENCES USER_TB(USER_NO),
    
    CONSTRAINT UK_USER_CHAT_ROOM UNIQUE (USER_A_NO, USER_B_NO)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE USER_CHAT_ROOM IS '회원 간 1:1 채팅방';
COMMENT ON COLUMN USER_CHAT_ROOM.ROOM_ID IS '회원 채팅방 고유 ID';
COMMENT ON COLUMN USER_CHAT_ROOM.USER_A_NO IS '회원 A 번호';
COMMENT ON COLUMN USER_CHAT_ROOM.USER_B_NO IS '회원 B 번호';
COMMENT ON COLUMN USER_CHAT_ROOM.LAST_READ_A_AT IS '회원 A 마지막 읽은 시간';
COMMENT ON COLUMN USER_CHAT_ROOM.LAST_READ_B_AT IS '회원 B 마지막 읽은 시간';
COMMENT ON COLUMN USER_CHAT_ROOM.CREATED_AT IS '채팅방 생성 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE USER_CHAT_ROOM_SEQ;

CREATE SEQUENCE USER_CHAT_ROOM_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
