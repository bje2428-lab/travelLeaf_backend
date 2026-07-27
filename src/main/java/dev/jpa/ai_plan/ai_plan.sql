-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE AI_PLAN CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE AI_PLAN (
    AI_PLAN_ID     NUMBER(20)    NOT NULL,   -- AI 결과 ID
    REQUEST_ID     NUMBER(20)    NOT NULL,   -- AI 실행 이력
    BATCH_ID       NUMBER(20)    NOT NULL,   -- 여행(체크리스트 묶음)
    USER_NO        NUMBER(20)    NOT NULL,   -- 사용자

    RESULT_JSON    CLOB          NOT NULL,   -- AI가 생성한 결과(JSON)

    CREATED_AT     DATE          DEFAULT SYSDATE,

    PRIMARY KEY (AI_PLAN_ID),
    FOREIGN KEY (REQUEST_ID) REFERENCES AI_REQUEST(REQUEST_ID),
    FOREIGN KEY (BATCH_ID)   REFERENCES CHECKLIST_BATCH(BATCH_ID),
    FOREIGN KEY (USER_NO)    REFERENCES USER_TB(USER_NO)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE AI_PLAN IS 'AI가 생성한 결과 데이터(여행 일정, 위험 안내 등)';
COMMENT ON COLUMN AI_PLAN.AI_PLAN_ID IS 'AI 결과 고유 ID';
COMMENT ON COLUMN AI_PLAN.REQUEST_ID IS 'AI 실행 요청 ID';
COMMENT ON COLUMN AI_PLAN.BATCH_ID IS '체크리스트/여행 묶음 ID';
COMMENT ON COLUMN AI_PLAN.USER_NO IS '사용자 번호';
COMMENT ON COLUMN AI_PLAN.RESULT_JSON IS 'AI 생성 결과(JSON)';
COMMENT ON COLUMN AI_PLAN.CREATED_AT IS 'AI 결과 생성 시각';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE AI_PLAN_SEQ;

CREATE SEQUENCE AI_PLAN_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
