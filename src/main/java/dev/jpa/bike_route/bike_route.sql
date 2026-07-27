-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE BIKE_ROUTE CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE BIKE_ROUTE (
    ROUTE_ID             NUMBER(20)    NOT NULL,   -- 자전거길 ID
    ROUTE_NAME           VARCHAR2(200) NOT NULL,   -- 자전거길 명
    REGION               VARCHAR2(100),            -- 광역 지역 (서울, 경기, 강원 등)
    CITY                 VARCHAR2(100),            -- 시/군/구

    TOTAL_DISTANCE_KM    NUMBER(6,2),               -- 전체 거리(km)
    ESTIMATED_TIME_MIN   NUMBER(6),                 -- 예상 소요 시간(분)

    DESCRIPTION           CLOB,                     -- 자전거길 설명
    HIGHLIGHTS            CLOB,                     -- 감상 포인트 (JSON 배열)
    FOOD_INFO             CLOB,                     -- 먹거리 정보 (JSON 배열)
    TIPS                  CLOB,                     -- 여행 팁 (JSON 배열)

    CREATED_AT            DATE DEFAULT SYSDATE,

    PRIMARY KEY (ROUTE_ID)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE BIKE_ROUTE IS '공공데이터 기반 자전거길 메타 정보';
COMMENT ON COLUMN BIKE_ROUTE.ROUTE_ID IS '자전거길 ID';
COMMENT ON COLUMN BIKE_ROUTE.ROUTE_NAME IS '자전거길 명';
COMMENT ON COLUMN BIKE_ROUTE.REGION IS '광역 지역';
COMMENT ON COLUMN BIKE_ROUTE.CITY IS '시/군/구';
COMMENT ON COLUMN BIKE_ROUTE.TOTAL_DISTANCE_KM IS '전체 주행 거리(km)';
COMMENT ON COLUMN BIKE_ROUTE.ESTIMATED_TIME_MIN IS '예상 소요 시간(분)';
COMMENT ON COLUMN BIKE_ROUTE.DESCRIPTION IS '자전거길 상세 설명';
COMMENT ON COLUMN BIKE_ROUTE.HIGHLIGHTS IS '감상 포인트(JSON 배열)';
COMMENT ON COLUMN BIKE_ROUTE.FOOD_INFO IS '먹거리 정보(JSON 배열)';
COMMENT ON COLUMN BIKE_ROUTE.TIPS IS '여행 팁(JSON 배열)';
COMMENT ON COLUMN BIKE_ROUTE.CREATED_AT IS '데이터 생성 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE BIKE_ROUTE_SEQ;

CREATE SEQUENCE BIKE_ROUTE_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
