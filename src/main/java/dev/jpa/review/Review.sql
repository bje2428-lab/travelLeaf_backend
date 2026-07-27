------------------------------------------------------------
-- 1. 기존 REVIEW 테이블 및 시퀀스 삭제
------------------------------------------------------------
DROP TABLE REVIEW CASCADE CONSTRAINTS;
DROP SEQUENCE REVIEW_SEQ;


------------------------------------------------------------
-- 2. REVIEW 테이블 생성
--   (시 / 군구 / 장소명 직접 입력 기반 리뷰)
------------------------------------------------------------
CREATE TABLE REVIEW (
    review_id     NUMBER PRIMARY KEY,          -- 리뷰 PK
    city          VARCHAR2(20) NOT NULL,        -- 시 (예: 서울)
    district      VARCHAR2(20) NOT NULL,        -- 군/구 (예: 마포구)
    place_name    VARCHAR2(100) NOT NULL,       -- 장소명 (직접 입력)
    user_id       VARCHAR2(50) NOT NULL,        -- 작성자 ID
    rating        NUMBER(1) NOT NULL,           -- 별점 (1~5)
    content       VARCHAR2(2000) NOT NULL,      -- 리뷰 내용
    created_at    DATE DEFAULT SYSDATE,         -- 작성일
    updated_at    DATE                          -- 수정일
);


------------------------------------------------------------
-- 3. 컬럼 설명 (COMMENT)
------------------------------------------------------------
COMMENT ON TABLE REVIEW IS '시/군구/장소 직접 입력 기반 리뷰 테이블';

COMMENT ON COLUMN REVIEW.review_id  IS '리뷰 ID (PK)';
COMMENT ON COLUMN REVIEW.city       IS '시';
COMMENT ON COLUMN REVIEW.district   IS '군/구';
COMMENT ON COLUMN REVIEW.place_name IS '사용자가 입력한 장소명';
COMMENT ON COLUMN REVIEW.user_id    IS '작성자 ID';
COMMENT ON COLUMN REVIEW.rating     IS '별점 (1~5)';
COMMENT ON COLUMN REVIEW.content    IS '리뷰 내용';
COMMENT ON COLUMN REVIEW.created_at IS '리뷰 작성일';
COMMENT ON COLUMN REVIEW.updated_at IS '리뷰 수정일';


------------------------------------------------------------
-- 4. 외래키 설정 (USER_TB 연동)
--    ※ 장소는 자유 입력이므로 FK 설정하지 않음
------------------------------------------------------------
ALTER TABLE REVIEW
ADD CONSTRAINT fk_review_user
FOREIGN KEY (user_id)
REFERENCES USER_TB(user_id);


------------------------------------------------------------
-- 5. 시퀀스 생성
------------------------------------------------------------
CREATE SEQUENCE REVIEW_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


------------------------------------------------------------
-- 6. CRUD SQL 예시
------------------------------------------------------------

-- ⭐ CREATE (리뷰 등록)
INSERT INTO REVIEW (
    review_id,
    city,
    district,
    place_name,
    user_id,
    rating,
    content,
    created_at
)
VALUES (
    REVIEW_SEQ.NEXTVAL,
    :city,          -- 예: '서울'
    :district,      -- 예: '마포구'
    :place_name,    -- 예: '망원한강공원'
    :user_id,       -- 예: 'test01'
    :rating,        -- 1~5
    :content,
    SYSDATE
);


-- ⭐ READ (시 + 군구 기준 리뷰 목록)
SELECT *
FROM REVIEW
WHERE city = :city
  AND district = :district
ORDER BY created_at DESC;


-- ⭐ READ (특정 장소 리뷰 목록)
SELECT *
FROM REVIEW
WHERE city = :city
  AND district = :district
  AND place_name = :place_name
ORDER BY created_at DESC;


-- ⭐ READ (단일 리뷰 조회)
SELECT *
FROM REVIEW
WHERE review_id = :review_id;


-- ⭐ UPDATE (본인만 수정 가능)
UPDATE REVIEW
SET
    rating = :rating,
    content = :content,
    updated_at = SYSDATE
WHERE review_id = :review_id
  AND user_id = :user_id;


-- ⭐ DELETE (본인만 삭제 가능)
DELETE FROM REVIEW
WHERE review_id = :review_id
  AND user_id = :user_id;


/* =========================
   댓글
========================= */
CREATE TABLE REVIEW_COMMENT (
  COMMENT_ID   NUMBER(19) PRIMARY KEY,
  REVIEW_ID    NUMBER(19) NOT NULL,
  USER_ID      VARCHAR2(50) NOT NULL,
  CONTENT      VARCHAR2(1000) NOT NULL,

  IS_DELETED   NUMBER(1) DEFAULT 0 NOT NULL,
  DELETED_AT   TIMESTAMP NULL,

  CREATED_AT   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  UPDATED_AT   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE SEQUENCE REVIEW_COMMENT_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX IDX_REVIEW_COMMENT_REVIEW_ID ON REVIEW_COMMENT(REVIEW_ID);
CREATE INDEX IDX_REVIEW_COMMENT_USER_ID ON REVIEW_COMMENT(USER_ID);

/* =========================
   댓글 좋아요 (사용자 1회 제한)
========================= */
CREATE TABLE REVIEW_COMMENT_LIKE (
  LIKE_ID     NUMBER(19) PRIMARY KEY,
  COMMENT_ID  NUMBER(19) NOT NULL,
  USER_ID     VARCHAR2(50) NOT NULL,
  CREATED_AT  TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE SEQUENCE REVIEW_COMMENT_LIKE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;

ALTER TABLE REVIEW_COMMENT_LIKE
ADD CONSTRAINT UK_COMMENT_USER UNIQUE (COMMENT_ID, USER_ID);

CREATE INDEX IDX_COMMENT_LIKE_COMMENT_ID ON REVIEW_COMMENT_LIKE(COMMENT_ID);

/* FK는 원하면 추가 (댓글 삭제/리뷰 삭제 전략 따라 CASCADE 고민) */
-- ALTER TABLE REVIEW_COMMENT
-- ADD CONSTRAINT FK_COMMENT_REVIEW FOREIGN KEY (REVIEW_ID) REFERENCES REVIEW(REVIEW_ID);

-- ALTER TABLE REVIEW_COMMENT_LIKE
-- ADD CONSTRAINT FK_LIKE_COMMENT FOREIGN KEY (COMMENT_ID) REFERENCES REVIEW_COMMENT(COMMENT_ID);


CREATE TABLE REVIEW_REPORTS (
    report_id        NUMBER PRIMARY KEY,
    reporter_id      VARCHAR2(50) NOT NULL,
    user_id          VARCHAR2(50),

    report_category  VARCHAR2(50),
    reason           CLOB,
    evidence_url     VARCHAR2(500),

    status           VARCHAR2(20)
                     DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','IN_REVIEW','APPROVED','REJECTED')),

    ai_score         NUMBER(5,2),
    ai_model         VARCHAR2(50),
    ai_detected      CHAR(1),

    created_at       DATE DEFAULT SYSDATE,
    processed_at     DATE,

    review_id        NUMBER(10) NOT NULL
);

CREATE SEQUENCE SEQ_REVIEW_REPORTS START WITH 1 INCREMENT BY 1 NOCACHE;

ALTER TABLE REVIEW_REPORTS
ADD CONSTRAINT fk_review_reports_reporter
FOREIGN KEY (reporter_id) REFERENCES USER_TB(USER_ID);

ALTER TABLE REVIEW_REPORTS
ADD CONSTRAINT fk_review_reports_manager
FOREIGN KEY (user_id) REFERENCES USER_TB(USER_ID);

ALTER TABLE REVIEW_REPORTS
ADD CONSTRAINT fk_review_reports_review
FOREIGN KEY (review_id) REFERENCES REVIEW(REVIEW_ID);

ALTER TABLE REVIEW_REPORTS
ADD CONSTRAINT uq_review_report UNIQUE (reporter_id, review_id);

CREATE INDEX idx_review_reports_status   ON REVIEW_REPORTS(status);
CREATE INDEX idx_review_reports_category ON REVIEW_REPORTS(report_category);
CREATE INDEX idx_review_reports_review   ON REVIEW_REPORTS(review_id);
CREATE INDEX idx_review_reports_reporter ON REVIEW_REPORTS(reporter_id);
CREATE TABLE REVIEW_COMMENT_REPORTS (
    report_id        NUMBER PRIMARY KEY,
    reporter_id      VARCHAR2(50) NOT NULL,
    user_id          VARCHAR2(50),

    report_category  VARCHAR2(50),
    reason           CLOB,
    evidence_url     VARCHAR2(500),

    status           VARCHAR2(20)
                     DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','IN_REVIEW','APPROVED','REJECTED')),

    ai_score         NUMBER(5,2),
    ai_model         VARCHAR2(50),
    ai_detected      CHAR(1),

    created_at       DATE DEFAULT SYSDATE,
    processed_at     DATE,

    comment_id       NUMBER(10) NOT NULL
);

CREATE SEQUENCE SEQ_REVIEW_COMMENT_REPORTS START WITH 1 INCREMENT BY 1 NOCACHE;

ALTER TABLE REVIEW_COMMENT_REPORTS
ADD CONSTRAINT fk_review_comment_reports_reporter
FOREIGN KEY (reporter_id) REFERENCES USER_TB(USER_ID);

ALTER TABLE REVIEW_COMMENT_REPORTS
ADD CONSTRAINT fk_review_comment_reports_manager
FOREIGN KEY (user_id) REFERENCES USER_TB(USER_ID);

-- ✅ 리뷰 댓글 테이블명/PK컬럼명에 맞춰 수정 필요!
-- 아래는 예시: REVIEW_COMMENT(comment_id)
ALTER TABLE REVIEW_COMMENT_REPORTS
ADD CONSTRAINT fk_review_comment_reports_comment
FOREIGN KEY (comment_id) REFERENCES REVIEW_COMMENT(comment_id);

ALTER TABLE REVIEW_COMMENT_REPORTS
ADD CONSTRAINT uq_review_comment_report UNIQUE (reporter_id, comment_id);

CREATE INDEX idx_review_comment_reports_status   ON REVIEW_COMMENT_REPORTS(status);
CREATE INDEX idx_review_comment_reports_category ON REVIEW_COMMENT_REPORTS(report_category);
CREATE INDEX idx_review_comment_reports_comment  ON REVIEW_COMMENT_REPORTS(comment_id);
CREATE INDEX idx_review_comment_reports_reporter ON REVIEW_COMMENT_REPORTS(reporter_id);



ALTER TABLE REVIEW_COMMENT_REPORTS DROP CONSTRAINT FK_REVIEW_COMMENT_REPORTS_COMMENT;

ALTER TABLE REVIEW_COMMENT_REPORTS
ADD CONSTRAINT FK_REVIEW_COMMENT_REPORTS_COMMENT
FOREIGN KEY (COMMENT_ID)
REFERENCES REVIEW_COMMENT(COMMENT_ID)
ON DELETE CASCADE;

ALTER TABLE REVIEW ADD (IS_DELETED NUMBER(1) DEFAULT 0 NOT NULL);
ALTER TABLE REVIEW ADD (DELETED_AT TIMESTAMP);
commit;

UPDATE REVIEW
SET IS_DELETED = 0
WHERE IS_DELETED IS NULL;

COMMIT;

ALTER TABLE REVIEW
MODIFY (IS_DELETED DEFAULT 0);