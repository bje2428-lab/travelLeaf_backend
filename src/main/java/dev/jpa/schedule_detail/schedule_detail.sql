-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE SCHEDULE_DETAIL CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE SCHEDULE_DETAIL (
    DETAIL_ID          NUMBER(20)    NOT NULL,   -- 일정 상세 ID
    SCHEDULE_ID        NUMBER(20)    NOT NULL,   -- 상위 일정 ID

    PLACE_ID           NUMBER(20),               -- 장소 ID (등록된 장소)
    PLACE_NAME         VARCHAR2(300),            -- 장소명 (AI/사용자 입력용)

    -- ✅ 추가: 지역/도시 FK (날씨/분석용)
    REGION_ID          NUMBER(20),               -- 지역 ID (REGION)
    CITY_ID            NUMBER(20),               -- 도시 ID (CITY)

    DAY_NUMBER         NUMBER(5)     NOT NULL,   -- 여행 N일차
    ORDER_IN_DAY       NUMBER(5)     NOT NULL,   -- 하루 내 순서

    STOP_TYPE          VARCHAR2(10)  NOT NULL,   -- 정차 유형 (START, END, WAYPOINT, COURSE, STAY)

    START_TIME         DATE,                       -- 시작 일시
    END_TIME           DATE,                       -- 종료 일시

    COST               NUMBER(10),                -- 비용
    MEMO               VARCHAR2(1000),            -- 메모

    DISTANCE_KM        NUMBER(10),                -- 이동/코스 거리 (km)

    CREATED_AT         DATE DEFAULT SYSDATE,      -- 생성 시각
    UPDATED_AT         DATE DEFAULT SYSDATE,      -- 수정 시각

    CONSTRAINT PK_SCHEDULE_DETAIL PRIMARY KEY (DETAIL_ID),

    CONSTRAINT FK_SCHEDULE_DETAIL_SCHEDULE
        FOREIGN KEY (SCHEDULE_ID)
        REFERENCES SCHEDULE (SCHEDULE_ID),

    CONSTRAINT FK_SCHEDULE_DETAIL_PLACE
        FOREIGN KEY (PLACE_ID)
        REFERENCES PLACES (PLACE_ID),

    -- ✅ 추가 FK (※ 아래 REGION/CITY 테이블/PK명은 너희 스키마 확인 필요)
    CONSTRAINT FK_SCHD_DETAIL_REGION
        FOREIGN KEY (REGION_ID)
        REFERENCES REGION (REGION_ID),

    CONSTRAINT FK_SCHD_DETAIL_CITY
        FOREIGN KEY (CITY_ID)
        REFERENCES CITY (CITY_ID)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE SCHEDULE_DETAIL
IS '여행 일정 상세 정보 (장소 및 이동/코스 단위)';

COMMENT ON COLUMN SCHEDULE_DETAIL.DETAIL_ID
IS '일정 상세 고유 ID';

COMMENT ON COLUMN SCHEDULE_DETAIL.SCHEDULE_ID
IS '상위 일정 ID';

COMMENT ON COLUMN SCHEDULE_DETAIL.PLACE_ID
IS '등록된 장소 ID (지도 API 사용 시)';

COMMENT ON COLUMN SCHEDULE_DETAIL.PLACE_NAME
IS '장소명 (PLACE_ID 없을 때 필수, AI 일정/사용자 입력 공용)';

COMMENT ON COLUMN SCHEDULE_DETAIL.REGION_ID
IS '지역 ID (REGION FK, 날씨/분석용)';

COMMENT ON COLUMN SCHEDULE_DETAIL.CITY_ID
IS '도시 ID (CITY FK, 날씨/분석용)';

COMMENT ON COLUMN SCHEDULE_DETAIL.DAY_NUMBER
IS '여행 일차 (Day 1, Day 2 …)';

COMMENT ON COLUMN SCHEDULE_DETAIL.ORDER_IN_DAY
IS '해당 일차 내 순서';

COMMENT ON COLUMN SCHEDULE_DETAIL.STOP_TYPE
IS '정차 유형 (START, END, WAYPOINT, COURSE, STAY)';

COMMENT ON COLUMN SCHEDULE_DETAIL.START_TIME
IS '일정 시작 일시';

COMMENT ON COLUMN SCHEDULE_DETAIL.END_TIME
IS '일정 종료 일시';

COMMENT ON COLUMN SCHEDULE_DETAIL.COST
IS '예상 비용';

COMMENT ON COLUMN SCHEDULE_DETAIL.MEMO
IS '일정 메모';

COMMENT ON COLUMN SCHEDULE_DETAIL.DISTANCE_KM
IS '이동 또는 코스 거리 (km)';

COMMENT ON COLUMN SCHEDULE_DETAIL.CREATED_AT
IS '상세 일정 생성 시각';

COMMENT ON COLUMN SCHEDULE_DETAIL.UPDATED_AT
IS '상세 일정 수정 시각';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE SCHEDULE_DETAIL_SEQ;

CREATE SEQUENCE SCHEDULE_DETAIL_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;

-----------------------------------------------------------
-- 삭제 오류 수정(ON DELETE CASCADE)
-- ※ 여기 SYS_C0031225 는 환경마다 달라서 "확정" 불가.
--    너희 DB에서 실제 FK 제약명 조회 후 그 이름으로 DROP 해야 함.
-----------------------------------------------------------

/* 확인 */
SELECT constraint_name, delete_rule
FROM user_constraints
WHERE table_name = 'SCHEDULE_DETAIL'
  AND constraint_type = 'R';

-- 위 조회 결과에서 SCHEDULE_ID FK 이름을 확인한 뒤 아래처럼 진행:
-- (예시) ALTER TABLE SCHEDULE_DETAIL DROP CONSTRAINT FK_SCHEDULE_DETAIL_SCHEDULE;

ALTER TABLE SCHEDULE_DETAIL
  ADD CONSTRAINT FK_SCHD_DETAIL_SCHEDULE
  FOREIGN KEY (SCHEDULE_ID)
  REFERENCES SCHEDULE(SCHEDULE_ID)
  ON DELETE CASCADE;

-----------------------------------------------------------
-- (선택) 인덱스 권장 (조회/날씨분석용)
-----------------------------------------------------------
CREATE INDEX IDX_SCHD_DETAIL_SCH_DAY
ON SCHEDULE_DETAIL (SCHEDULE_ID, DAY_NUMBER, ORDER_IN_DAY);

CREATE INDEX IDX_SCHD_DETAIL_REGION_CITY
ON SCHEDULE_DETAIL (REGION_ID, CITY_ID);
