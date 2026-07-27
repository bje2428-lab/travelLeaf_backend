-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE AI_REQUEST CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE AI_REQUEST (
    REQUEST_ID     NUMBER(20)    NOT NULL,   -- AI 요청 ID
    USER_NO        NUMBER(20)    NOT NULL,   -- 요청 사용자
    AI_TYPE        VARCHAR2(30)  NOT NULL,   -- PLAN / WEATHER / PLACE
    INPUT_SUMMARY  CLOB,                     -- 프롬프트 요약
    CREATED_AT     DATE          DEFAULT SYSDATE,
    PRIMARY KEY (REQUEST_ID),
    FOREIGN KEY (USER_NO) REFERENCES USER_TB(USER_NO)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE AI_REQUEST IS 'AI 요청 이력';
COMMENT ON COLUMN AI_REQUEST.REQUEST_ID IS 'AI 요청 고유 ID';
COMMENT ON COLUMN AI_REQUEST.USER_NO IS 'AI 요청 사용자 번호';
COMMENT ON COLUMN AI_REQUEST.AI_TYPE IS 'AI 기능 유형 (PLAN/WEATHER/PLACE)';
COMMENT ON COLUMN AI_REQUEST.INPUT_SUMMARY IS 'AI 입력 요약 정보';
COMMENT ON COLUMN AI_REQUEST.CREATED_AT IS 'AI 요청 생성 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE AI_REQUEST_SEQ;

CREATE SEQUENCE AI_REQUEST_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
