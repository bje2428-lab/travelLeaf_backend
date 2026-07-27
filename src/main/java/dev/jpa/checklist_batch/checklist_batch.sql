-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE CHECKLIST_BATCH CASCADE CONSTRAINTS;
DROP SEQUENCE CHECKLIST_BATCH_SEQ;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE CHECKLIST_BATCH (
    BATCH_ID        NUMBER(20)     NOT NULL,
    USER_NO         NUMBER(10)     NOT NULL,
    TITLE           VARCHAR2(200)  NOT NULL,
    ROUTE_REGIONS   CLOB           NOT NULL, -- 여행 지역
    ROUTE_CITIES    CLOB, -- 여행 도시
    ROUTE_WAYPOINTS CLOB, -- 가고싶은 경유지
    START_POINT     VARCHAR2(100),  -- 여행 시작 장소
    END_POINT       VARCHAR2(100),  -- 여행 종료 장소
    START_DATETIME  VARCHAR2(16)   NOT NULL,  -- 여행 시작 일시
    END_DATETIME    VARCHAR2(16)   NOT NULL,  -- 여행 종료 일시
    CREATED_AT      DATE           DEFAULT SYSDATE,   -- 생성일

    -------------------------------------------------------
    -- CONSTRAINT
    -------------------------------------------------------
    CONSTRAINT PK_CHECKLIST_BATCH PRIMARY KEY (BATCH_ID),
    CONSTRAINT FK_CHECKLIST_BATCH_USER
        FOREIGN KEY (USER_NO)
        REFERENCES USER_TB(USER_NO),

    -- 날짜 포맷 강제 (yyyy-MM-ddTHH:mm)
    CONSTRAINT CK_BATCH_START_DATETIME
        CHECK (
            REGEXP_LIKE(
                START_DATETIME,
                '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}$'
            )
        ),

    CONSTRAINT CK_BATCH_END_DATETIME
        CHECK (
            REGEXP_LIKE(
                END_DATETIME,
                '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}$'
            )
        )
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE CHECKLIST_BATCH IS
'체크리스트 저장 단위(여행 1회, 다중 지역/도시 경로 + 경유지 포함)';

COMMENT ON COLUMN CHECKLIST_BATCH.BATCH_ID IS
'체크리스트/AI 실행 단위 ID';

COMMENT ON COLUMN CHECKLIST_BATCH.USER_NO IS
'사용자 번호';

COMMENT ON COLUMN CHECKLIST_BATCH.TITLE IS
'여행 제목(사용자 입력)';

COMMENT ON COLUMN CHECKLIST_BATCH.ROUTE_REGIONS IS
'여행 시/도 경로(JSON 배열, 순서 중요)';

COMMENT ON COLUMN CHECKLIST_BATCH.ROUTE_CITIES IS
'여행 도시 경로(JSON 배열, 순서 중요)';

COMMENT ON COLUMN CHECKLIST_BATCH.ROUTE_WAYPOINTS IS
'유저가 가고 싶은 장소(경유지, POI 등, JSON 배열)';

COMMENT ON COLUMN CHECKLIST_BATCH.START_POINT IS
'출발 지점(도시/랜드마크, 선택)';

COMMENT ON COLUMN CHECKLIST_BATCH.END_POINT IS
'도착 지점(도시/랜드마크, 선택)';

COMMENT ON COLUMN CHECKLIST_BATCH.START_DATETIME IS
'여행 시작 일시 (yyyy-MM-ddTHH:mm)';

COMMENT ON COLUMN CHECKLIST_BATCH.END_DATETIME IS
'여행 종료 일시 (yyyy-MM-ddTHH:mm)';

COMMENT ON COLUMN CHECKLIST_BATCH.CREATED_AT IS
'생성 시각';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
CREATE SEQUENCE CHECKLIST_BATCH_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9999999999
    NOCACHE
    NOCYCLE;
