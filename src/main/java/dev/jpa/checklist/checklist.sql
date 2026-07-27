DROP TABLE CHECKLIST CASCADE CONSTRAINTS;

CREATE TABLE CHECKLIST (
    ITEM_ID       NUMBER(20)     NOT NULL,
    CATEGORY      VARCHAR2(100),             -- 항목 분류(여행스타일/분위기 등)
    ITEM_NAME     VARCHAR2(100)  NOT NULL,  -- 체크리스트 항목 이름
    DESCRIPTION   VARCHAR2(255),            -- 항목 설명
    CREATED_AT    DATE,              -- 등록일
    PRIMARY KEY (ITEM_ID)
);

COMMENT ON TABLE CHECKLIST IS '사용자 체크리스트 항목 테이블';
COMMENT ON COLUMN CHECKLIST.ITEM_ID IS '체크리스트 항목 ID';
COMMENT ON COLUMN CHECKLIST.CATEGORY IS '항목 카테고리';
COMMENT ON COLUMN CHECKLIST.ITEM_NAME IS '항목 이름';
COMMENT ON COLUMN CHECKLIST.DESCRIPTION IS '항목 설명';
COMMENT ON COLUMN CHECKLIST.CREATED_AT IS '등록일';


DROP SEQUENCE CHECKLIST_SEQ;

-- 시퀀스 생성
CREATE SEQUENCE CHECKLIST_SEQ
  START WITH 1                -- 시작 번호
  INCREMENT BY 1              -- 증가값
  MAXVALUE 9999999999         -- 최대값
  CACHE 2                     -- 메모리 캐시
  NOCYCLE;                     -- 1부터 다시 시작하지 않음


-- -----------------------
-- CRUD
-- -----------------------

-- 리스트 추가
INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (1, 'Mood', '여유로운', '차량과 사람 통행이 적고 여유롭게 라이딩을 즐길 수 있는 분위기 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (2, 'Mood', '활기찬', '라이더가 많고 에너지가 넘치는 코스를 선호하며 활발한 분위기 즐김', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (3, 'Mood', '감성적인', '풍경이 아름답고 사진 찍기 좋은 감성적인 라이딩 코스를 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (4, 'Mood', '도전적인', '업힐과 장거리 등 난이도 높은 코스를 도전하는 라이딩을 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (5, 'Mood', '힐링', '자연 풍경을 느끼며 천천히 달리는 힐링 라이딩을 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (6, 'Mood', '경치 중심', '전망 좋은 길, 바다·강·산 풍경이 돋보이는 코스를 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (7, 'Mood', '탐험', '잘 알려지지 않은 코스나 새로운 길을 찾아가는 탐험형 라이딩을 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (8, 'Mood', '여행형', '카페 방문, 지역 탐방 등 여행처럼 즐기는 라이딩 스타일을 선호', sysdate);

----------
INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (101, 'People', '혼자 여행', '혼자서 여행', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (102, 'People', '친구와 여행', '지인 및 친구들과 함께 여행', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (103, 'People', '커플 여행', '연인과 함께 하는 여행', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (104, 'People', '매칭 여행', '일정과 장소가 맞는 사람들과 매칭해서 함께 여행', sysdate);
----------
INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (201, 'Activity', '초심자 코스', '평탄하고 가벼운 라이딩 코스로 누구나 쉽게 즐길 수 있는 코스', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (202, 'Activity', '중급자 코스', '약간의 오르막과 내리막이 포함된 적당한 난이도의 라이딩 코스', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (203, 'Activity', '고급 코스', '장거리 또는 경사도가 높은 구간을 포함한 고난도 라이딩 코스', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (204, 'Activity', '산악 자전거 코스', '비포장산길, 험지 등을 주행하는 MTB 전용 코스', sysdate);
----------
INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (301, 'Food', '현지 맛집', '지역 맛집 탐방 중심 여행', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (302, 'Food', '카페 투어', '감성 카페와 디저트 중심', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (303, 'Food', '해산물', '해산물 기반의 음식 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (304, 'Food', '채식', '채소 위주의 음식 선호', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (305, 'Food', '매운 음식', '강한 향신료와 매운 음식 선호', sysdate);
----------
INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (401, 'Stay', '코스 접근성 좋음', '주요 라이딩 코스와 가까워 이동이 편리한 숙소', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (402, 'Stay', '조용한 숙소', '라이딩 후 휴식하기 좋은 조용한 주변 환경의 숙소', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (403, 'Stay', '편의시설 우수', '주차장, 근처 편의점, 음식점 등이 잘 갖춰진 숙소', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (404, 'Stay', '동행 적합', '가족 또는 친구와 함께 머물기 좋은 넓은 객실 또는 다인실 제공 숙소', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (405, 'Stay', '가성비', '라이더에게 부담 없는 가격 대비 만족스러운 숙소', sysdate);

INSERT INTO CHECKLIST (ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT)
VALUES (406, 'Stay', '전망 좋은 숙소', '휴식 시 풍경이 좋은 객실 또는 테라스를 제공하는 숙소', sysdate);

commit;


-- 전체 조회
SELECT ITEM_ID, CATEGORY, ITEM_NAME, DESCRIPTION, CREATED_AT
FROM CHECKLIST
ORDER BY ITEM_ID;

-- 그룹별 조회
SELECT ITEM_ID, ITEM_NAME, DESCRIPTION, CREATED_AT
FROM CHECKLIST
WHERE CATEGORY = 'Mood'
ORDER BY ITEM_ID;



-- 수정
UPDATE CHECKLIST
SET ITEM_NAME = '잔잔한 분위기',
    DESCRIPTION = '잔잔하고 조용한 여행을 선호',
    CATEGORY = 'Mood'
WHERE ITEM_ID = 1;



-- 삭제
DELETE FROM CHECKLIST
WHERE ITEM_ID = 1;

