---------------------------------------------------------
-- 💡 POSTS_EMBEDDINGS FULL SCRIPT (Oracle)
-- 테이블 생성 + 제약조건 + 트리거 + CRUD + 관리 쿼리
---------------------------------------------------------

---------------------------------------------------------
-- 1️⃣ 기존 테이블 있으면 삭제 (선택)
---------------------------------------------------------
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE POSTS_EMBEDDINGS CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/
---------------------------------------------------------

---------------------------------------------------------
-- 2️⃣ 테이블 생성
---------------------------------------------------------
CREATE TABLE POSTS_EMBEDDINGS (
    post_id      NUMBER(10)      NOT NULL,
    embedding    CLOB            NOT NULL,
    updated_at   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    
    CONSTRAINT pk_posts_embeddings
        PRIMARY KEY (post_id),

    CONSTRAINT fk_posts_embeddings_post
        FOREIGN KEY (post_id)
        REFERENCES POSTS(post_id)
        ON DELETE CASCADE
);
---------------------------------------------------------

---------------------------------------------------------
-- 3️⃣ updated_at 자동 갱신 트리거
---------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_posts_embeddings_upd
BEFORE UPDATE ON POSTS_EMBEDDINGS
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/
---------------------------------------------------------

---------------------------------------------------------
-- 4️⃣ CRUD SQL (원하는 거 그대로 써서 실행하면 됨)
---------------------------------------------------------

-- ▶️ INSERT (게시글 생성 후 임베딩 넣기)
-- 변수 사용 가능: &POST_ID / &EMBED
INSERT INTO POSTS_EMBEDDINGS (
    post_id,
    embedding,
    updated_at
) VALUES (
    &POST_ID,
    &EMBED,
    SYSTIMESTAMP
);


-- ▶️ SELECT (특정 게시글 조회)
SELECT post_id, embedding, updated_at
FROM POSTS_EMBEDDINGS
WHERE post_id = &POST_ID;


-- ▶️ SELECT ALL (관리용)
SELECT post_id, updated_at
FROM POSTS_EMBEDDINGS
ORDER BY updated_at DESC;


-- ▶️ UPDATE (임베딩 재생성)
UPDATE POSTS_EMBEDDINGS
SET embedding = &NEW_EMBED
WHERE post_id = &POST_ID;


-- ▶️ DELETE (임베딩만 삭제)
DELETE FROM POSTS_EMBEDDINGS
WHERE post_id = &POST_ID;
---------------------------------------------------------

---------------------------------------------------------
-- 5️⃣ UPSERT (있으면 UPDATE / 없으면 INSERT)
---------------------------------------------------------
MERGE INTO POSTS_EMBEDDINGS T
USING (
    SELECT &POST_ID AS post_id,
           &EMBED AS embedding
    FROM dual
) S
ON (T.post_id = S.post_id)
WHEN MATCHED THEN
    UPDATE SET 
        T.embedding = S.embedding
WHEN NOT MATCHED THEN
    INSERT (post_id, embedding, updated_at)
    VALUES (S.post_id, S.embedding, SYSTIMESTAMP);
---------------------------------------------------------

---------------------------------------------------------
-- 6️⃣ 운영 관리용 (선택)
---------------------------------------------------------

-- 임베딩 없는 게시글 찾기
SELECT p.post_id
FROM POSTS p
LEFT JOIN POSTS_EMBEDDINGS e
     ON p.post_id = e.post_id
WHERE e.post_id IS NULL;


-- 오래된 임베딩 (30일 지난 것)
SELECT post_id
FROM POSTS_EMBEDDINGS
WHERE updated_at < SYSTIMESTAMP - INTERVAL '30' DAY;


-- 전체 삭제 (테스트용)
-- DELETE FROM POSTS_EMBEDDINGS;

---------------------------------------------------------
-- END
---------------------------------------------------------
