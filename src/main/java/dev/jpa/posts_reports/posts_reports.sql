---------------------------------------------------------
-- POSTS_REPORTS (게시글 신고)
---------------------------------------------------------
CREATE TABLE POSTS_REPORTS (
    report_id        NUMBER PRIMARY KEY,

    reporter_id      VARCHAR2(50) NOT NULL,   -- 신고자 (USER_TB.USER_ID)
    USER_ID          VARCHAR2(50),            -- 처리자(관리자, USER_TB.USER_ID)

    report_category  VARCHAR2(50),
    reason           CLOB,
    evidence_url     VARCHAR2(500),

    status           VARCHAR2(20)
                     DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','IN_REVIEW','APPROVED','REJECTED')),

    created_at       DATE DEFAULT SYSDATE,
    processed_at     DATE,

    post_id          NUMBER(10) NOT NULL      -- 신고 대상 게시글
);

---------------------------------------------------------
-- FK (무결성 보장)
---------------------------------------------------------
ALTER TABLE POSTS_REPORTS
ADD CONSTRAINT fk_posts_reports_reporter
FOREIGN KEY (reporter_id)
REFERENCES USER_TB(USER_ID);

ALTER TABLE POSTS_REPORTS
ADD CONSTRAINT fk_posts_reports_admin
FOREIGN KEY (USER_ID)
REFERENCES USER_TB(USER_ID);

-- ⚠️ post_id FK 는 이미 있는 경우 많음 → 필요시만 실행
-- ALTER TABLE POSTS_REPORTS
-- ADD CONSTRAINT fk_posts_reports_post
-- FOREIGN KEY (post_id)
-- REFERENCES POSTS(post_id);


---------------------------------------------------------
-- 중복 신고 방지
---------------------------------------------------------
ALTER TABLE POSTS_REPORTS
ADD CONSTRAINT uq_posts_report UNIQUE (reporter_id, post_id);


---------------------------------------------------------
-- INDEX (성능)
---------------------------------------------------------
CREATE INDEX idx_posts_reports_status   ON POSTS_REPORTS(status);
CREATE INDEX idx_posts_reports_category ON POSTS_REPORTS(report_category);
CREATE INDEX idx_posts_reports_post     ON POSTS_REPORTS(post_id);
CREATE INDEX idx_posts_reports_reporter ON POSTS_REPORTS(reporter_id);


---------------------------------------------------------
-- SEQUENCE (PK 생성)
---------------------------------------------------------
CREATE SEQUENCE SEQ_POSTS_REPORTS;


---------------------------------------------------------
-- CREATE (신고 등록)
---------------------------------------------------------
INSERT INTO POSTS_REPORTS (
    report_id,
    reporter_id,
    report_category,
    reason,
    evidence_url,
    post_id
)
VALUES (
    SEQ_POSTS_REPORTS.NEXTVAL,
    :reporter_id,
    :report_category,
    :reason,
    :evidence_url,
    :post_id
);


---------------------------------------------------------
-- READ
---------------------------------------------------------
SELECT * FROM POSTS_REPORTS ORDER BY created_at DESC;

SELECT * FROM POSTS_REPORTS
WHERE report_id = :report_id;

SELECT * FROM POSTS_REPORTS
WHERE post_id = :post_id
ORDER BY created_at DESC;

SELECT * FROM POSTS_REPORTS
WHERE reporter_id = :reporter_id;


---------------------------------------------------------
-- UPDATE (관리자 처리)
---------------------------------------------------------
UPDATE POSTS_REPORTS
SET status = :status,       -- 'IN_REVIEW','APPROVED','REJECTED'
    USER_ID = :admin_id,
    processed_at = SYSDATE
WHERE report_id = :report_id;


---------------------------------------------------------
-- DELETE
---------------------------------------------------------
DELETE FROM POSTS_REPORTS WHERE report_id = :report_id;
DELETE FROM POSTS_REPORTS WHERE post_id = :post_id;
DELETE FROM POSTS_REPORTS WHERE reporter_id = :reporter_id;
