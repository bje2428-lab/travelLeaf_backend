/* =========================================================
   기존 테이블 제거
   ========================================================= */

DROP TABLE AIPLACE_IMAGE CASCADE CONSTRAINTS;
DROP TABLE AIPLACE_RESULT CASCADE CONSTRAINTS;


/* =========================================================
   AI 장소 분석 결과 테이블
   ========================================================= */

CREATE TABLE AIPLACE_RESULT (
    RESULT_ID     NUMBER(20)      NOT NULL,
    PLACE_NAME    VARCHAR2(200),
    LATITUDE      NUMBER(10,6),
    LONGITUDE     NUMBER(10,6),
    COUNTRY       VARCHAR2(100),
    DESCRIPTION   VARCHAR2(1000),
    CONFIDENCE    NUMBER(3,2),
    SOURCE_API    VARCHAR2(50),
    CREATED_AT    DATE,
    PRIMARY KEY (RESULT_ID)
);

COMMENT ON TABLE AIPLACE_RESULT IS 'AI 이미지 분석으로 추출된 장소 정보 테이블';
COMMENT ON COLUMN AIPLACE_RESULT.RESULT_ID IS 'AI 장소 결과 ID';
COMMENT ON COLUMN AIPLACE_RESULT.PLACE_NAME IS 'AI가 인식한 장소명';
COMMENT ON COLUMN AIPLACE_RESULT.LATITUDE IS '위도';
COMMENT ON COLUMN AIPLACE_RESULT.LONGITUDE IS '경도';
COMMENT ON COLUMN AIPLACE_RESULT.COUNTRY IS '국가';
COMMENT ON COLUMN AIPLACE_RESULT.DESCRIPTION IS 'AI 분석 설명';
COMMENT ON COLUMN AIPLACE_RESULT.CONFIDENCE IS 'AI 인식 신뢰도';
COMMENT ON COLUMN AIPLACE_RESULT.SOURCE_API IS '사용한 AI API';
COMMENT ON COLUMN AIPLACE_RESULT.CREATED_AT IS '분석 일자';


/* =========================================================
   시퀀스 생성
   ========================================================= */

DROP SEQUENCE AIPLACE_RESULT_SEQ;

CREATE SEQUENCE AIPLACE_RESULT_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 2
  NOCYCLE;


/* =========================================================
   AI 장소 이미지 테이블
   ========================================================= */

CREATE TABLE AIPLACE_IMAGE (
    IMAGE_ID       NUMBER(20)     NOT NULL,
    RESULT_ID      NUMBER(20),
    IMAGE_PATH     VARCHAR2(500),
    ORIGINAL_NAME  VARCHAR2(255),
    CREATED_AT     DATE,
    PRIMARY KEY (IMAGE_ID),
    CONSTRAINT FK_AIPLACE_IMAGE
      FOREIGN KEY (RESULT_ID)
      REFERENCES AIPLACE_RESULT (RESULT_ID)
);

COMMENT ON TABLE AIPLACE_IMAGE IS 'AI 장소 분석에 사용된 원본 이미지 테이블';
COMMENT ON COLUMN AIPLACE_IMAGE.IMAGE_ID IS '이미지 ID';
COMMENT ON COLUMN AIPLACE_IMAGE.RESULT_ID IS 'AI 장소 결과 ID';
COMMENT ON COLUMN AIPLACE_IMAGE.IMAGE_PATH IS '서버 저장 이미지 경로';
COMMENT ON COLUMN AIPLACE_IMAGE.ORIGINAL_NAME IS '원본 파일명';
COMMENT ON COLUMN AIPLACE_IMAGE.CREATED_AT IS '업로드 일자';


/* =========================================================
   이미지 시퀀스
   ========================================================= */

DROP SEQUENCE AIPLACE_IMAGE_SEQ;

CREATE SEQUENCE AIPLACE_IMAGE_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 2
  NOCYCLE;


/* =========================================================
   CRUD - INSERT (AI 분석 결과)
   ========================================================= */

INSERT INTO AIPLACE_RESULT
(RESULT_ID, PLACE_NAME, LATITUDE, LONGITUDE, COUNTRY, DESCRIPTION, CONFIDENCE, SOURCE_API, CREATED_AT)
VALUES
(AIPLACE_RESULT_SEQ.NEXTVAL, 'Eiffel Tower', 48.858400, 2.294500, 'France',
 'AI landmark detection result', 0.92, 'GOOGLE_VISION', SYSDATE);

INSERT INTO AIPLACE_RESULT
(RESULT_ID, PLACE_NAME, LATITUDE, LONGITUDE, COUNTRY, CONFIDENCE, SOURCE_API, CREATED_AT)
VALUES
(AIPLACE_RESULT_SEQ.NEXTVAL, 'Statue of Liberty', 40.689247, -74.044502, 'USA',
 0.89, 'GOOGLE_VISION', SYSDATE);


/* =========================================================
   CRUD - INSERT (이미지 매핑)
   ========================================================= */

INSERT INTO AIPLACE_IMAGE
(IMAGE_ID, RESULT_ID, IMAGE_PATH, ORIGINAL_NAME, CREATED_AT)
VALUES
(AIPLACE_IMAGE_SEQ.NEXTVAL, 1, 'C:/upload/ai-place/eiffel.jpg', 'eiffel.jpg', SYSDATE);

INSERT INTO AIPLACE_IMAGE
(IMAGE_ID, RESULT_ID, IMAGE_PATH, ORIGINAL_NAME, CREATED_AT)
VALUES
(AIPLACE_IMAGE_SEQ.NEXTVAL, 2, 'C:/upload/ai-place/liberty.jpg', 'liberty.jpg', SYSDATE);


COMMIT;


/* =========================================================
   조회 예제
   ========================================================= */

-- 전체 AI 장소 결과 조회
SELECT RESULT_ID, PLACE_NAME, LATITUDE, LONGITUDE, CONFIDENCE, CREATED_AT
FROM AIPLACE_RESULT
ORDER BY RESULT_ID;

-- 특정 장소의 이미지 조회
SELECT I.IMAGE_ID, I.IMAGE_PATH, I.ORIGINAL_NAME
FROM AIPLACE_IMAGE I
WHERE I.RESULT_ID = 1;
