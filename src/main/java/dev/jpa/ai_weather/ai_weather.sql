-----------------------------------------------------------
-- DROP
-----------------------------------------------------------
DROP TABLE AI_WEATHER CASCADE CONSTRAINTS;

-----------------------------------------------------------
-- CREATE TABLE
-----------------------------------------------------------
CREATE TABLE AI_WEATHER (
    AI_WEATHER_ID   NUMBER(20)     NOT NULL,   -- 날씨 AI 분석 ID
    REQUEST_ID      NUMBER(20)     NOT NULL,   -- AI 요청 ID
    SCHEDULE_ID     NUMBER(20)     NOT NULL,   -- 일정 ID
    DETAIL_ID       NUMBER(20),                -- 일정 상세 ID
    TARGET_DATE     DATE           NOT NULL,   -- 분석 대상 날짜

    LAT             NUMBER(10,6),              -- 위도
    LNG             NUMBER(10,6),              -- 경도

    REGION_ID       NUMBER(20),                -- 날씨 분석 기준 광역 지역 ID
    CITY_ID         NUMBER(20),                -- 날씨 분석 기준 시군구 ID
    REGION_NAME     VARCHAR2(100),             -- 날씨 분석 대상 지역명(행정기준: 광역+시군구)
    WEATHER_TYPE    VARCHAR2(20),              -- MOVE / STAY / START / END / DAY
    WIND_SPEED      NUMBER(5,2),               -- 풍속 (m/s)
    PRECIPITATION   NUMBER(6,2),               -- 강수량 (mm)
    PRECIP_PROB     NUMBER(5,2),               -- 강수확률 (%): 제공되면 저장, 없으면 NULL
    TEMP_MAX        NUMBER(5,2),               -- 최고기온 (°C)
    TEMP_MIN        NUMBER(5,2),               -- 최저기온 (°C)
    AIR_GRADE VARCHAR2(20),                    -- 대기질 등급(좋음/보통/나쁨/매우나쁨)

    RISK_LEVEL      VARCHAR2(20)   NOT NULL,   -- VERY_SAFE / SAFE / CAUTION / WARNING / DANGER
    RISK_REASON     VARCHAR2(300),             -- 위험 사유(주의/경고/위험 중심)
    AI_MESSAGE      CLOB,                      -- 사용자 안내 문구
    CREATED_AT      DATE           DEFAULT SYSDATE,

    PRIMARY KEY (AI_WEATHER_ID),

    FOREIGN KEY (REQUEST_ID)  REFERENCES AI_REQUEST(REQUEST_ID),
    FOREIGN KEY (SCHEDULE_ID) REFERENCES SCHEDULE(SCHEDULE_ID),
    FOREIGN KEY (DETAIL_ID)   REFERENCES SCHEDULE_DETAIL(DETAIL_ID),
    FOREIGN KEY (REGION_ID)   REFERENCES REGION(REGION_ID),
    FOREIGN KEY (CITY_ID)     REFERENCES CITY(CITY_ID)
);

-----------------------------------------------------------
-- COMMENT
-----------------------------------------------------------
COMMENT ON TABLE AI_WEATHER IS '일정 기반 날씨 위험 분석';

COMMENT ON COLUMN AI_WEATHER.AI_WEATHER_ID IS '날씨 AI 분석 ID';
COMMENT ON COLUMN AI_WEATHER.REQUEST_ID IS 'AI 요청 ID';
COMMENT ON COLUMN AI_WEATHER.SCHEDULE_ID IS '분석 대상 일정 ID';
COMMENT ON COLUMN AI_WEATHER.DETAIL_ID IS '분석 대상 일정 상세 ID';
COMMENT ON COLUMN AI_WEATHER.TARGET_DATE IS '날씨 분석 날짜';

COMMENT ON COLUMN AI_WEATHER.LAT IS '위도';
COMMENT ON COLUMN AI_WEATHER.LNG IS '경도';

COMMENT ON COLUMN AI_WEATHER.REGION_ID IS '날씨 분석 기준 광역 지역 ID';
COMMENT ON COLUMN AI_WEATHER.CITY_ID IS '날씨 분석 기준 시군구 ID';
COMMENT ON COLUMN AI_WEATHER.REGION_NAME IS '날씨 분석 대상 지역명(행정기준: 광역+시군구)';
COMMENT ON COLUMN AI_WEATHER.WEATHER_TYPE IS '일정 내 위치 유형(MOVE / STAY / START / END / DAY)';

COMMENT ON COLUMN AI_WEATHER.WIND_SPEED IS '풍속(m/s)';
COMMENT ON COLUMN AI_WEATHER.PRECIPITATION IS '강수량(mm)';
COMMENT ON COLUMN AI_WEATHER.PRECIP_PROB IS '강수확률(%)';
COMMENT ON COLUMN AI_WEATHER.TEMP_MAX IS '최고기온(°C)';
COMMENT ON COLUMN AI_WEATHER.TEMP_MIN IS '최저기온(°C)';
COMMENT ON COLUMN AI_WEATHER.AIR_GRADE IS '대기질 등급(좋음/보통/나쁨/매우나쁨)';

COMMENT ON COLUMN AI_WEATHER.RISK_LEVEL IS '위험 수준(VERY_SAFE/SAFE/CAUTION/WARNING/DANGER)';
COMMENT ON COLUMN AI_WEATHER.RISK_REASON IS '위험 판단 사유';
COMMENT ON COLUMN AI_WEATHER.AI_MESSAGE IS 'AI 안내 문구';
COMMENT ON COLUMN AI_WEATHER.CREATED_AT IS '분석 생성 시간';

-----------------------------------------------------------
-- SEQUENCE
-----------------------------------------------------------
DROP SEQUENCE AI_WEATHER_SEQ;

CREATE SEQUENCE AI_WEATHER_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999
  NOCACHE
  NOCYCLE;
