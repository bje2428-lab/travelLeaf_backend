-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE CHECKLIST_USER CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE CHECKLIST_USER (
    CHECK_ID     NUMBER(10)    NOT NULL,
    USER_NO      NUMBER(10)    NOT NULL,
    ITEM_ID      NUMBER(10)    NOT NULL,
    BATCH_ID     NUMBER(20)    NOT NULL,
    CREATED_AT   DATE          DEFAULT SYSDATE,

    PRIMARY KEY (CHECK_ID),
    FOREIGN KEY (USER_NO)  REFERENCES USER_TB(USER_NO),
    FOREIGN KEY (ITEM_ID)  REFERENCES CHECKLIST(ITEM_ID),
    FOREIGN KEY (BATCH_ID) REFERENCES CHECKLIST_BATCH(BATCH_ID)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE CHECKLIST_USER IS '체크리스트 항목 선택 내역';
COMMENT ON COLUMN CHECKLIST_USER.CHECK_ID IS '고유 ID';
COMMENT ON COLUMN CHECKLIST_USER.USER_NO IS '사용자 번호';
COMMENT ON COLUMN CHECKLIST_USER.ITEM_ID IS '체크리스트 항목 ID';
COMMENT ON COLUMN CHECKLIST_USER.BATCH_ID IS '체크리스트 저장 단위 ID';
COMMENT ON COLUMN CHECKLIST_USER.CREATED_AT IS '선택 시각';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE CHECKLIST_USER_SEQ;

CREATE SEQUENCE CHECKLIST_USER_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 2
  NOCYCLE;
