/* ======================================================
   0. 기존 테이블 / 시퀀스 제거
====================================================== */
DROP TABLE QUIZ_ATTEMPT CASCADE CONSTRAINTS;
DROP TABLE QUIZ_DAY CASCADE CONSTRAINTS;
DROP TABLE QUIZ CASCADE CONSTRAINTS;

DROP SEQUENCE QUIZ_ATTEMPT_SEQ;
DROP SEQUENCE QUIZ_DAY_SEQ;
DROP SEQUENCE QUIZ_SEQ;

/* ======================================================
   1. QUIZ : 퀴즈 문제 은행
====================================================== */
CREATE TABLE QUIZ (
    QUIZ_ID       NUMBER PRIMARY KEY,
    CATEGORY      VARCHAR2(50),              -- 여행 / 자전거 / 안전
    QUESTION      VARCHAR2(500) NOT NULL,

    OPTION_1      VARCHAR2(200) NOT NULL,
    OPTION_2      VARCHAR2(200) NOT NULL,
    OPTION_3      VARCHAR2(200),
    OPTION_4      VARCHAR2(200),

    CORRECT_NO    NUMBER(1) CHECK (CORRECT_NO BETWEEN 1 AND 4),
    EXPLANATION   VARCHAR2(500),

    EXP_REWARD    NUMBER DEFAULT 10,
    CREATED_AT    DATE DEFAULT SYSDATE
);

CREATE SEQUENCE QUIZ_SEQ
START WITH 1 INCREMENT BY 1 NOCYCLE;

/* ======================================================
   2. QUIZ_DAY : 일차별 퀴즈 매핑
====================================================== */
CREATE TABLE QUIZ_DAY (
    QUIZ_DAY_ID NUMBER PRIMARY KEY,
    DAY_NO      NUMBER NOT NULL,        -- 1일차, 2일차
    QUIZ_ID     NUMBER NOT NULL,
    SORT_ORDER  NUMBER DEFAULT 1,
    ACTIVE_YN   CHAR(1) DEFAULT 'Y',

    CONSTRAINT FK_QUIZ_DAY_QUIZ
      FOREIGN KEY (QUIZ_ID)
      REFERENCES QUIZ(QUIZ_ID)
);

CREATE SEQUENCE QUIZ_DAY_SEQ
START WITH 1 INCREMENT BY 1 NOCYCLE;

/* ======================================================
   3. QUIZ_ATTEMPT : 사용자 퀴즈 풀이 기록
====================================================== */
CREATE TABLE QUIZ_ATTEMPT (
    ATTEMPT_ID   NUMBER PRIMARY KEY,
    USER_ID      NUMBER NOT NULL,
    QUIZ_ID      NUMBER NOT NULL,
    DAY_NO       NUMBER NOT NULL,
    IS_CORRECT   CHAR(1) CHECK (IS_CORRECT IN ('Y','N')),
    ATTEMPTED_AT DATE DEFAULT SYSDATE,

    CONSTRAINT FK_ATTEMPT_QUIZ
      FOREIGN KEY (QUIZ_ID)
      REFERENCES QUIZ(QUIZ_ID)
);

CREATE SEQUENCE QUIZ_ATTEMPT_SEQ
START WITH 1 INCREMENT BY 1 NOCYCLE;

/* ======================================================
   4. CREATE (INSERT)
====================================================== */

-- 퀴즈 문제 등록
INSERT INTO QUIZ VALUES (
  QUIZ_SEQ.NEXTVAL,
  '자전거',
  '장거리 라이딩 시 가장 중요한 준비물은?',
  '물과 보급식',
  '속도계',
  '고글',
  '장갑',
  1,
  '수분과 에너지 보충이 가장 중요합니다.',
  15,
  SYSDATE
);

INSERT INTO QUIZ VALUES (
  QUIZ_SEQ.NEXTVAL,
  '여행',
  '여행 일정 계획 시 가장 먼저 고려할 요소는?',
  '맛집',
  '숙소 위치',
  '사진 스팟',
  '기념품',
  2,
  '동선과 위치가 여행 만족도를 좌우합니다.',
  10,
  SYSDATE
);

-- 1일차 퀴즈 구성
INSERT INTO QUIZ_DAY VALUES (QUIZ_DAY_SEQ.NEXTVAL, 1, 1, 1, 'Y');
INSERT INTO QUIZ_DAY VALUES (QUIZ_DAY_SEQ.NEXTVAL, 1, 2, 2, 'Y');

-- 사용자 퀴즈 풀이 기록
INSERT INTO QUIZ_ATTEMPT VALUES (
  QUIZ_ATTEMPT_SEQ.NEXTVAL,
  1001,
  1,
  1,
  'Y',
  SYSDATE
);

/* ======================================================
   5. READ (SELECT)
====================================================== */

-- 전체 퀴즈 조회
SELECT * FROM QUIZ ORDER BY QUIZ_ID;

-- 특정 일차 퀴즈 조회
SELECT Q.QUIZ_ID, Q.QUESTION, Q.EXP_REWARD
FROM QUIZ_DAY D
JOIN QUIZ Q ON Q.QUIZ_ID = D.QUIZ_ID
WHERE D.DAY_NO = 1
  AND D.ACTIVE_YN = 'Y'
ORDER BY D.SORT_ORDER;

-- 사용자 오늘 퀴즈 풀이 여부 확인
SELECT COUNT(*) AS ATTEMPT_CNT
FROM QUIZ_ATTEMPT
WHERE USER_ID = 1001
  AND DAY_NO = 1;

-- 사용자 정답률 확인
SELECT
  COUNT(*) AS TOTAL,
  SUM(CASE WHEN IS_CORRECT = 'Y' THEN 1 ELSE 0 END) AS CORRECT
FROM QUIZ_ATTEMPT
WHERE USER_ID = 1001;

/* ======================================================
   6. UPDATE
====================================================== */

-- 퀴즈 문제 수정
UPDATE QUIZ
SET QUESTION = '장거리 라이딩 시 가장 필수적인 준비물은?',
    EXP_REWARD = 20
WHERE QUIZ_ID = 1;

-- 특정 퀴즈 비활성화
UPDATE QUIZ_DAY
SET ACTIVE_YN = 'N'
WHERE QUIZ_ID = 2
  AND DAY_NO = 1;

-- 퀴즈 풀이 결과 수정 (오답 → 정답)
UPDATE QUIZ_ATTEMPT
SET IS_CORRECT = 'Y'
WHERE ATTEMPT_ID = 1;

/* ======================================================
   7. DELETE
====================================================== */

-- 퀴즈 풀이 기록 삭제
DELETE FROM QUIZ_ATTEMPT
WHERE ATTEMPT_ID = 1;

-- 특정 일차 퀴즈 구성 삭제
DELETE FROM QUIZ_DAY
WHERE DAY_NO = 2;

-- 퀴즈 문제 삭제 (연관된 DAY/ATTEMPT 먼저 삭제 필요)
DELETE FROM QUIZ
WHERE QUIZ_ID = 2;

COMMIT;
