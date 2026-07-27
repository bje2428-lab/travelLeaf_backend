/**********************************/
/* Table Name: 카테고리 */
/**********************************/
-- 장르를 카테고리 그룹 테이블로 처리하지 않고 컬럼으로 처리함 ★
DROP TABLE CATE;
DROP TABLE CATE CASCADE CONSTRAINTS;

CREATE TABLE CATE(
  CATENO NUMBER(10) NOT NULL,
  GRP VARCHAR2(30) NOT NULL,
  NAME VARCHAR2(30) NOT NULL,
  CNT NUMBER(7) DEFAULT 0 NOT NULL,
  SEQNO NUMBER(5) DEFAULT 1 NOT NULL,
  VISIBLE CHAR(1) DEFAULT 'N' NOT NULL,
  RDATE VARCHAR(19) NOT NULL,
  PRIMARY KEY(CATENO)
);

COMMENT ON TABLE cate is '카테고리';
COMMENT ON COLUMN cate.CATENO is '카테고리 번호';
COMMENT ON COLUMN cate.GRP is '그룹 이름';
COMMENT ON COLUMN cate.NAME is '카테고리 이름';
COMMENT ON COLUMN cate.CNT is '관련 자료수';
COMMENT ON COLUMN cate.SEQNO is '출력 순서';
COMMENT ON COLUMN cate.VISIBLE is '출력 모드';
COMMENT ON COLUMN cate.RDATE is '등록일';

DROP SEQUENCE CATE_SEQ;

CREATE SEQUENCE CATE_SEQ
START WITH 1 -- 시작 번호
INCREMENT BY 1 -- 증가값
MAXVALUE 9999999999 -- 최대값: 9999999999 --> NUMBER(10) 대응
CACHE 2 -- 2번은 메모리에서만 계산
NOCYCLE; -- 다시 1부터 생성되는 것을 방지


--> CREATE
-- 대분류
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '영화', '--', 0, 1, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '영화', 'SF', 0, 2, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '영화', '공포', 0, 3, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '영화', '드라마', 0, 4, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '여행', '--', 0, 101, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '여행', '국내', 0, 102, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '여행', '해외', 0, 103, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '캠핑', '--', 0, 201, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '캠핑', '경기도', 0, 202, 'Y', SYSDATE);
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '캠핑', '강원도', 0, 203, 'Y', SYSDATE);

--> SELECT 목록
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
ORDER BY seqno ASC;
    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE              
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
         1 영화                           --                                      0          1 Y 2025-10-29 05:50:08
         2 영화                           SF                                      0          2 Y 2025-10-29 05:50:08
         3 영화                           공포                                    0          3 Y 2025-10-29 05:50:08
         4 영화                           드라마                                  0          4 Y 2025-10-29 05:50:08
         5 여행                           --                                      0        101 Y 2025-10-29 05:50:08
         6 여행                           국내                                    0        102 Y 2025-10-29 05:50:08
         7 여행                           해외                                    0        103 Y 2025-10-29 05:50:08
         8 캠핑                           --                                      0        201 Y 2025-10-29 05:50:08
         9 캠핑                           경기도                                  0        201 Y 2025-10-29 05:50:08
        10 캠핑                           강원도                                  0        201 Y 2025-10-29 05:50:08
        
commit;

SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
ORDER BY cateno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- -------------------- ------------------------------ ---------- ---------- - -------------------
1 드라마 K드라마 0 1 Y 2025-03-18 12:03:49
2 드라마 미드 0 1 Y 2025-03-18 12:10:59
3 영화 SF 0 1 Y 2025-03-18 12:11:46
4 영화 드라마 0 1 Y 2025-03-18 12:12:52
5 개발 JAVA 0 1 Y 2025-03-18 12:12:52

--> SELECT 조회
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno = 1;

CATENO GRP NAME CNT SEQNO V RDATE
---------- -------------------- ------------------------------ ---------- ---------- - -------------------
1 드라마 K드라마 0 1 Y 2025-03-18 12:03:49

--> UPDATE
UPDATE cate
SET grp='여행', name='국내', seqno=1, visible='Y', rdate=SYSDATE
WHERE cateno=5;

SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno = 5;

CATENO GRP NAME CNT SEQNO V RDATE
---------- -------------------- ------------------------------ ---------- ---------- - -------------------
5 여행 국내 0 1 Y 2025-03-18 12:18:43

--> DELETE
DELETE FROM cate WHERE cateno=5;

--> COUNT(*)
SELECT COUNT(*) as cnt FROM cate;

CNT
----------
4
COMMIT;


DELETE FROM cate;
COMMIT;

-- 데이터 준비
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(cate_seq.nextval, '여행', '--', 0, 0, 'Y', sysdate);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(cate_seq.nextval, '까페', '--', 0, 0, 'Y', sysdate);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(cate_seq.nextval, '영화', '--', 0, 0, 'Y', sysdate);

-- 목록 변경됨
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
28 여행 -- 0 0 Y 2025-03-26 10:16:57
30 영화 -- 0 0 Y 2025-03-26 10:16:57
29 까페 -- 0 0 Y 2025-03-26 10:16:57

-- 출력 우선순위 낮춤
UPDATE cate SET seqno=seqno+1 WHERE cateno=28;
UPDATE cate SET seqno=seqno+1 WHERE cateno=28;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
29 까페 -- 0 0 Y 2025-03-26 10:16:57
30 영화 -- 0 0 Y 2025-03-26 10:16:57
28 여행 -- 0 2 Y 2025-03-26 10:16:57

UPDATE cate SET seqno=seqno+1 WHERE cateno=30;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
29 까페 -- 0 0 Y 2025-03-26 10:16:57
30 영화 -- 0 1 Y 2025-03-26 10:16:57
28 여행 -- 0 2 Y 2025-03-26 10:16:57
UPDATE cate SET seqno=seqno+1 WHERE cateno=29;
UPDATE cate SET seqno=seqno+1 WHERE cateno=29;
UPDATE cate SET seqno=seqno+1 WHERE cateno=29;
UPDATE cate SET seqno=seqno+1 WHERE cateno=29;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
28 여행 -- 0 1 Y 2025-03-26 10:16:57
30 영화 -- 0 2 Y 2025-03-26 10:16:57
29 까페 -- 0 4 Y 2025-03-26 10:16:57
-- 출력 우선순위 높임
UPDATE cate SET seqno=seqno-1 WHERE cateno=29;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
28 여행 -- 0 1 Y 2025-03-26 10:16:57
30 영화 -- 0 2 Y 2025-03-26 10:16:57
29 까페 -- 0 3 Y 2025-03-26 10:16:57

COMMIT;

SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
ORDER BY seqno ASC;


-- 카테고리 공개 설정
UPDATE cate SET visible='Y' WHERE cateno=1;

-- 카테고리 비공개 설정
UPDATE cate SET visible='N' WHERE cateno=1;


COMMIT;


--------------------------------------------------------------------------------
-- 대분류, 중분류 처리
--------------------------------------------------------------------------------
-- 회원/비회원에게 공개할 카테고리 그룹(대분류) 목록
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
31 개발 -- 0 1 Y 2025-03-27 12:56:03
34 개발 JAVA 0 2 Y 2025-03-27 12:48:03
35 개발 Python 0 3 Y 2025-03-27 12:49:04
36 개발 LLM 0 4 Y 2025-03-27 12:49:18
32 여행 -- 0 101 Y 2025-03-27 12:54:01
37 여행 국내 0 102 Y 2025-03-27 12:54:45
38 여행 해외 0 103 N 2025-03-27 12:55:00
33 영화 -- 0 201 N 2025-03-27 12:54:18
39 영화 국내 0 202 N 2025-03-27 12:55:14
40 영화 해외 0 203 N 2025-03-27 12:55:21


SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate WHERE name='--' ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
31 개발 -- 0 1 Y 2025-03-27 12:56:03
32 여행 -- 0 101 Y 2025-03-27 12:54:01
33 영화 -- 0 201 N 2025-03-27 12:54:18

-- 공개된 대분류만 출력(*)
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate 
WHERE name='--' AND visible='Y' ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
31 개발 -- 0 1 Y 2025-03-27 12:56:03
32 여행 -- 0 101 Y 2025-03-27 12:54:01

-- 회원/비회원에게 공개할 카테고리(중분류) 목록
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate 
WHERE grp='영화' AND visible='Y' ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
31 개발 -- 0 1 Y 2025-03-27 12:56:03
34 개발 JAVA 0 2 Y 2025-03-27 12:48:03
35 개발 Python 0 3 Y 2025-03-27 12:49:04
36 개발 LLM 0 4 Y 2025-03-27 12:49:18

-- 개발 그룹의 중분류 출력(*)
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE grp='영화' AND name != '--' AND visible = 'Y'
ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
34 개발 JAVA 0 2 Y 2025-03-27 12:48:03
35 개발 Python 0 3 Y 2025-03-27 12:49:04
36 개발 LLM 0 4 Y 2025-03-27 12:49:18

-- 여행 그룹의 중분류 출력
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE grp='여행' AND name != '--' AND visible = 'Y'
ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
37 여행 국내 0 102 Y 2025-03-27 12:54:45

-- 카테고리 목록 출력
SELECT cateno, grp FROM cate WHERE name = '--' ORDER BY seqno ASC;
CATENO GRP
---------- --------------------
8 까페
7 여행
9 영화

SELECT grp FROM cate WHERE name = '--' ORDER BY seqno ASC; -- 권장
GENRE
--------------------
까페
여행
영화

-- SELECT DISTINCT cateno, grp FROM cate ORDER BY seqno ASC; X

-- FWGHSRO
-- SELECT DISTINCT grp FROM cate ORDER BY seqno ASC; X
SELECT DISTINCT grp FROM cate;
GENRE
--------------------
까페
영화
여행


-- 검색
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('까페') || '%') OR (UPPER(name) LIKE '%' || UPPER('까페') || '%')
ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- -------------------- ------------------------------ ---------- ---------- - -------------------
8 까페 -- 0 1 Y 2024-09-13 10:04:04
10 까페 강화도 0 10 Y 2024-09-19 04:19:41
12 까페 김포 0 11 Y 2024-09-19 04:19:50
15 까페 추천 0 12 Y 2024-09-19 04:20:21
-- '카테고리 그룹'을 제외한 경우
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (name != '--') AND ((UPPER(grp) LIKE '%' || UPPER('까페') || '%') OR (UPPER(name) LIKE '%' || UPPER('까페') || '%'))
ORDER BY seqno ASC;

CATENO GRP NAME CNT SEQNO V RDATE
---------- -------------------- ------------------------------ ---------- ---------- - -------------------
10 까페 강화도 0 10 Y 2024-09-19 04:19:41
12 까페 김포 0 11 Y 2024-09-19 04:19:50
15 까페 추천 0 12 Y 2024-09-19 04:20:21


-- -----------------------------------------------------------------------------
-- 페이징: 정렬 -> ROWNUM -> 분할
-- -----------------------------------------------------------------------------
-- ① 정렬
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('영화') || '%') OR (UPPER(name) LIKE '%' || UPPER('영화') || '%')
ORDER BY seqno ASC;

-- ② 정렬 -> ROWNUM
SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('영화') || '%') OR (UPPER(name) LIKE '%' || UPPER('영화') || '%')
ORDER BY seqno ASC
);

-- ③ 정렬 -> ROWNUM -> 분할
SELECT cateno, grp, name, cnt, seqno, visible, rdate, r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('영화') || '%') OR (UPPER(name) LIKE '%' || UPPER('영화') || '%')
ORDER BY seqno ASC
)
)
WHERE r >= 1 AND r <= 3;

CATENO grp NAME CNT SEQNO V RDATE R
---------- -------------------- ------------------------------ ---------- ---------- - ------------------- ----------
8 까페 -- 0 1 Y 2024-09-13 10:04:04 1
10 까페 강화도2 0 10 Y 2024-09-24 05:42:54 2
12 까페 김포 0 11 Y 2024-09-19 04:19:50 3
SELECT cateno, grp, name, cnt, seqno, visible, rdate, r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('영화') || '%') OR (UPPER(name) LIKE '%' || UPPER('영화') || '%')
ORDER BY seqno ASC
)
)
WHERE r >= 4 AND r <= 6;

CATENO grp NAME CNT SEQNO V RDATE R
---------- -------------------- ------------------------------ ---------- ---------- - ------------------- ----------
15 까페 추천 0 12 Y 2024-09-19 04:20:21 4
17 까페 남한산성 0 15 Y 2024-09-24 04:01:35 5
18 까페 영종도 0 16 Y 2024-09-24 04:02:56 6

SELECT cateno, grp, name, cnt, seqno, visible, rdate, r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
FROM (
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('영화') || '%') OR (UPPER(name) LIKE '%' || UPPER('영화') || '%')
ORDER BY seqno ASC
)
)
WHERE r >= 7 AND r <= 9;

CATENO grp NAME CNT SEQNO V RDATE R
---------- -------------------- ------------------------------ ---------- ---------- - ------------------- ----------
19 까페 빵까페 0 19 Y 2024-09-24 04:08:50 7


-- 대분류 증가, cateno 1번의 대분류명을 찾음
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno=5;

UPDATE cate SET cnt = cnt + 1 WHERE grp='영화' and name='--';

-- 중분류 증가
UPDATE cate SET cnt = cnt + 1 WHERE cateno=5

-- 대분류 감소, cateno 1번의 대분류명을 찾음
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno=5;

UPDATE cate SET cnt = cnt - 1 WHERE grp='영화' and name='--';

-- 중분류 감소
UPDATE cate SET cnt = cnt - 1 WHERE cateno=1

-- 갯수 전달받아 대분류 감소
UPDATE cate SET cnt = cnt - 5 WHERE grp='영화' and name='--';
rollback;
