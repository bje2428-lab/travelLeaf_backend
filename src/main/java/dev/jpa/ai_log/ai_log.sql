-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE AI_LOG CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE AI_LOG (
    LOG_ID         NUMBER(20)    NOT NULL,   -- AI 로그 ID
    REQUEST_ID     NUMBER(20)    NOT NULL,   -- AI 요청 ID
    STATUS         VARCHAR2(20)  NOT NULL,   -- SUCCESS / FAIL
    LATENCY_MS     NUMBER(10),               -- 응답 시간(ms)
    ERROR_MESSAGE  CLOB,                     -- 에러 메시지
    CREATED_AT     DATE          DEFAULT SYSDATE,
    PRIMARY KEY (LOG_ID),
    FOREIGN KEY (REQUEST_ID) REFERENCES AI_REQUEST(REQUEST_ID)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE AI_LOG IS 'AI 실행 로그';
COMMENT ON COLUMN AI_LOG.LOG_ID IS 'AI 로그 고유 ID';
COMMENT ON COLUMN AI_LOG.REQUEST_ID IS '연결된 AI 요청 ID';
COMMENT ON COLUMN AI_LOG.STATUS IS 'AI 실행 상태';
COMMENT ON COLUMN AI_LOG.LATENCY_MS IS 'AI 응답 시간(ms)';
COMMENT ON COLUMN AI_LOG.ERROR_MESSAGE IS 'AI 오류 메시지';
COMMENT ON COLUMN AI_LOG.CREATED_AT IS '로그 생성 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE AI_LOG_SEQ;

CREATE SEQUENCE AI_LOG_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
