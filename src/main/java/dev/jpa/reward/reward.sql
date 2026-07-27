/* ======================================================
   0. 기존 테이블 / 시퀀스 정리
====================================================== */
DROP TABLE REWARD_LOG CASCADE CONSTRAINTS;
DROP TABLE REWARD_MASTER CASCADE CONSTRAINTS;
DROP TABLE USER_STATUS CASCADE CONSTRAINTS;
DROP TABLE LEVEL_POLICY CASCADE CONSTRAINTS;

DROP SEQUENCE REWARD_LOG_SEQ;
DROP SEQUENCE REWARD_SEQ;


/* ======================================================
   1. 유저 현재 상태 (경험치 / 레벨 / 포인트)
====================================================== */
CREATE TABLE USER_STATUS (
    USER_NO        NUMBER PRIMARY KEY,
    CURRENT_EXP    NUMBER DEFAULT 0,
    CURRENT_LEVEL  NUMBER DEFAULT 1,
    CURRENT_POINT  NUMBER DEFAULT 0,
    UPDATED_AT     DATE DEFAULT SYSDATE
);

COMMENT ON TABLE USER_STATUS IS '유저 경험치/레벨/포인트 상태';
COMMENT ON COLUMN USER_STATUS.USER_NO IS '유저 내부 번호';
COMMENT ON COLUMN USER_STATUS.CURRENT_EXP IS '현재 경험치';
COMMENT ON COLUMN USER_STATUS.CURRENT_LEVEL IS '현재 레벨';
COMMENT ON COLUMN USER_STATUS.CURRENT_POINT IS '현재 포인트';


/* ======================================================
   2. 레벨 기준표
====================================================== */
CREATE TABLE LEVEL_POLICY (
    LEVEL_NO        NUMBER PRIMARY KEY,
    REQUIRED_EXP    NUMBER NOT NULL,
    LEVEL_NAME      VARCHAR2(50)
);

COMMENT ON TABLE LEVEL_POLICY IS '레벨 기준 정책';
COMMENT ON COLUMN LEVEL_POLICY.REQUIRED_EXP IS '누적 필요 경험치';

INSERT INTO LEVEL_POLICY VALUES (1, 0,    '초보 라이더');
INSERT INTO LEVEL_POLICY VALUES (2, 100,  '입문 라이더');
INSERT INTO LEVEL_POLICY VALUES (3, 300,  '중급 라이더');
INSERT INTO LEVEL_POLICY VALUES (4, 600,  '고급 라이더');
INSERT INTO LEVEL_POLICY VALUES (5, 1000, '마스터 라이더');


/* ======================================================
   3. 보상 정의 마스터
====================================================== */
CREATE TABLE REWARD_MASTER (
    REWARD_ID      NUMBER PRIMARY KEY,
    REWARD_TYPE    VARCHAR2(20),      -- EXP / POINT / BADGE
    REWARD_VALUE   NUMBER,
    REWARD_NAME    VARCHAR2(100),
    DESCRIPTION    VARCHAR2(255)
);

COMMENT ON TABLE REWARD_MASTER IS '보상 정의 테이블';

CREATE SEQUENCE REWARD_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- 보상 예시
INSERT INTO REWARD_MASTER VALUES (
  REWARD_SEQ.NEXTVAL,
  'EXP',
  10,
  '퀴즈 정답 보상',
  '퀴즈 1문제 정답 시 경험치 지급'
);

INSERT INTO REWARD_MASTER VALUES (
  REWARD_SEQ.NEXTVAL,
  'EXP',
  50,
  '일일 퀴즈 올클리어',
  '하루 퀴즈 전체 완료 보상'
);


/* ======================================================
   4. 보상 지급 기록 (퀴즈 풀이 기록 포함)
====================================================== */
CREATE TABLE REWARD_LOG (
    REWARD_LOG_ID   NUMBER PRIMARY KEY,
    USER_NO         NUMBER NOT NULL,
    REWARD_ID       NUMBER NOT NULL,
    SOURCE_TYPE     VARCHAR2(30),    -- QUIZ / DAILY / EVENT
    SOURCE_KEY      VARCHAR2(50),    -- QUIZ_101 / DAY_1 / EVENT_xxx
    REWARD_VALUE    NUMBER,
    CREATED_AT      DATE DEFAULT SYSDATE
);

COMMENT ON TABLE REWARD_LOG IS '보상 지급 및 퀴즈 기록 통합 로그';

CREATE SEQUENCE REWARD_LOG_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


/* ======================================================
   5. 유저 최초 생성 시 상태 초기화 예시
====================================================== */
-- USER_NO = 1 인 유저 상태 생성
INSERT INTO USER_STATUS (USER_NO)
VALUES (1);


/* ======================================================
   6. 퀴즈 정답 처리 예시 (퀴즈 기록 = 보상 로그)
====================================================== */
INSERT INTO REWARD_LOG (
    REWARD_LOG_ID,
    USER_NO,
    REWARD_ID,
    SOURCE_TYPE,
    SOURCE_KEY,
    REWARD_VALUE
) VALUES (
    REWARD_LOG_SEQ.NEXTVAL,
    1,
    1,
    'QUIZ',
    'QUIZ_101',
    10
);

-- 경험치 누적
UPDATE USER_STATUS
SET CURRENT_EXP = CURRENT_EXP + 10,
    UPDATED_AT = SYSDATE
WHERE USER_NO = 1;

-- 레벨 자동 계산
UPDATE USER_STATUS
SET CURRENT_LEVEL = (
    SELECT MAX(LEVEL_NO)
    FROM LEVEL_POLICY
    WHERE REQUIRED_EXP <= CURRENT_EXP
)
WHERE USER_NO = 1;


/* ======================================================
   7. 하루 1회 퀴즈 제한 체크
====================================================== */
-- 오늘 DAY_1 퀴즈를 풀었는지 확인
SELECT COUNT(*)
FROM REWARD_LOG
WHERE USER_NO = 1
  AND SOURCE_TYPE = 'QUIZ'
  AND SOURCE_KEY = 'DAY_1'
  AND TRUNC(CREATED_AT) = TRUNC(SYSDATE);


/* ======================================================
   8. 유저 상태 조회
====================================================== */
SELECT
    U.USER_NO,
    U.CURRENT_LEVEL,
    U.CURRENT_EXP,
    U.CURRENT_POINT
FROM USER_STATUS U;


/* ======================================================
   9. 보상 지급 이력 조회
====================================================== */
SELECT
    R.REWARD_LOG_ID,
    R.USER_NO,
    M.REWARD_NAME,
    R.SOURCE_TYPE,
    R.SOURCE_KEY,
    R.REWARD_VALUE,
    R.CREATED_AT
FROM REWARD_LOG R
JOIN REWARD_MASTER M
  ON R.REWARD_ID = M.REWARD_ID
ORDER BY R.CREATED_AT DESC;


COMMIT;
