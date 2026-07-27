-- 테이블 삭제
DROP TABLE USER_TB CASCADE CONSTRAINTS;

ALTER TABLE USER_TB DROP COLUMN BIRTH_YEAR;
ALTER TABLE USER_TB ADD BIRTH VARCHAR2(10);


-- 1. 기존 컬럼 삭제
ALTER TABLE USER_TB
DROP COLUMN BIRTH_YEAR;

commit;

ALTER TABLE USER_TB
ADD BIRTH DATE;

-- 2. 모든 행에 기본값 넣기
UPDATE USER_TB
SET BIRTH = TO_DATE('2000-01-01', 'YYYY-MM-DD');

-- 3. 확인
SELECT USER_ID, BIRTH FROM USER_TB;

DESC USER_TB;

commit;

commit;

UPDATE USER_TB
SET GRADE = 2
WHERE USER_ID IN ('user1', 'user2');

commit;



-- 테이블 생성
CREATE TABLE USER (
    USER_ID        VARCHAR2(50)      PRIMARY KEY,   -- 사람/외부 식별자 (예: 'user1')
    USER_NO        NUMBER            UNIQUE NOT NULL, -- 내부 순번(SEQ)
    EMAIL          VARCHAR2(50)       UNIQUE NOT NULL,
    PASSWORD       VARCHAR2(50)       NOT NULL,
    NICKNAME       VARCHAR2(50)       UNIQUE NOT NULL,
    PROFILE_IMAGE  VARCHAR2(100),
    PHONE          VARCHAR2(20)       UNIQUE,
    BIRTH_YEAR     NUMBER(4),
    GENDER         VARCHAR2(10),
    CREATED_AT     DATE              DEFAULT SYSDATE,
    STATUS         VARCHAR2(20)       DEFAULT 'ACTIVE'
);

-- USER_NO 시퀀스 삭제 (존재하면 삭제)
DROP SEQUENCE USER_NO_SEQ;

-- NO 시퀀스 생성 (USER_NO에 사용할 시퀀스)
CREATE SEQUENCE USER_NO_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- -----------------------
-- CRUD 예시 (INSERT 시 USER_NO_SEQ.NEXTVAL을 직접 사용)
-- -----------------------

-- 회원 추가 (ID는 사람이 정함, NO는 시퀀스로 자동 부여)
INSERT INTO USER_TB (
    USER_ID, USER_NO, EMAIL, PASSWORD, NICKNAME,
    PROFILE_IMAGE, PHONE, BIRTH_YEAR, GENDER
) VALUES (
    'user1', USER_NO_SEQ.NEXTVAL, 'test@example.com', 'password123', '닉네임',
    'profile.jpg', '01012345678', 2000, 'M'
);

-- 전체 조회
SELECT * FROM USER_TB;

-- 특정 회원 조회 (ID로 조회)
SELECT * FROM USER_TB WHERE USER_ID = 'user1';

-- 수정 예시 (ID 기준)
UPDATE USER_TB
SET EMAIL = 'newemail@example.com',
    NICKNAME = '새닉네임'
WHERE USER_ID = 'user1';

-- 삭제 예시 (ID 기준)
DELETE FROM USER_TB WHERE USER_ID = 'user1';
