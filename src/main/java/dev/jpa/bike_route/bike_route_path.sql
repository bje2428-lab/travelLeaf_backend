-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE BIKE_ROUTE_PATH CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE BIKE_ROUTE_PATH (
    PATH_ID     NUMBER(20)    NOT NULL,   -- 경로 좌표 ID
    ROUTE_ID    NUMBER(20)    NOT NULL,   -- 자전거길 ID
    SEQ         NUMBER(10)    NOT NULL,   -- 경로 순서

    LAT         NUMBER(10,7)  NOT NULL,   -- 위도
    LNG         NUMBER(10,7)  NOT NULL,   -- 경도

    PRIMARY KEY (PATH_ID),
    FOREIGN KEY (ROUTE_ID) REFERENCES BIKE_ROUTE(ROUTE_ID) ON DELETE CASCADE
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE BIKE_ROUTE_PATH IS '자전거길 경로 좌표 정보';
COMMENT ON COLUMN BIKE_ROUTE_PATH.PATH_ID IS '경로 좌표 ID';
COMMENT ON COLUMN BIKE_ROUTE_PATH.ROUTE_ID IS '자전거길 ID';
COMMENT ON COLUMN BIKE_ROUTE_PATH.SEQ IS '좌표 순서';
COMMENT ON COLUMN BIKE_ROUTE_PATH.LAT IS '위도';
COMMENT ON COLUMN BIKE_ROUTE_PATH.LNG IS '경도';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE BIKE_ROUTE_PATH_SEQ;

CREATE SEQUENCE BIKE_ROUTE_PATH_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
  