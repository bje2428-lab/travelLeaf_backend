-- ============================================
-- 1. 기존 삭제
-- ============================================
DROP TABLE NOTICE CASCADE CONSTRAINTS;
DROP SEQUENCE notice_seq;

-- ============================================
-- 2. 새 NOTICE 테이블 (user_id → VARCHAR2로 맞춤)
-- ============================================
CREATE TABLE NOTICE (
    notice_id       NUMBER PRIMARY KEY,
    user_id         VARCHAR2(50) NOT NULL,      -- FK (작성자)
    is_fixed        CHAR(1) DEFAULT 'N',
    title           VARCHAR2(200) NOT NULL,
    content         CLOB NOT NULL,
    view_count      NUMBER DEFAULT 0,
    file_url        VARCHAR2(500),
    category        VARCHAR2(100),
    created_at      DATE DEFAULT SYSDATE,
    updated_at      DATE
);

-- ============================================
-- 3. COMMENT
-- ============================================
COMMENT ON TABLE NOTICE IS '공지사항';
COMMENT ON COLUMN NOTICE.user_id IS '작성자(FK), USER_TB.user_id';

-- ============================================
-- 4. FK: USER_TB 연결
-- ============================================
ALTER TABLE NOTICE
ADD CONSTRAINT fk_notice_user
FOREIGN KEY (user_id)
REFERENCES USER_TB(user_id);

-- ============================================
-- 5. SEQUENCE
-- ============================================
CREATE SEQUENCE notice_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- ============================================
-- 6. INSERT (admin_id → user_id로 변경)
-- ============================================
INSERT INTO NOTICE (
    notice_id, user_id, is_fixed, title, content, file_url, category, created_at, updated_at
) VALUES (
    notice_seq.NEXTVAL,
    :user_id,         -- 관리자 or 일반 유저 (grade로 권한 체크)
    :is_fixed,
    :title,
    :content,
    :file_url,
    :category,
    SYSDATE,
    NULL
);
