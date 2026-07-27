/* =========================
   DROP (있으면 제거)
========================= */
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE PLACES_DESCRIPTION CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE PLACES CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_PLACES';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -2289 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_PLACES_DESC';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -2289 THEN RAISE; END IF;
END;
/

/* =========================
   CREATE: PLACES
   - place_id: 카카오 id를 그대로 저장 (NUMBER)
   - SEQUENCE 사용 안 함
========================= */
CREATE TABLE PLACES (
    place_id        NUMBER          PRIMARY KEY,   -- ✅ 카카오 id 그대로

    name            VARCHAR2(200)    NOT NULL,
    category        VARCHAR2(100),

    region_id       NUMBER,
    city_id         NUMBER,

    address         VARCHAR2(300),
    lat             NUMBER,
    lng             NUMBER,

    rating          NUMBER,

    source_type     VARCHAR2(50),                  -- 예: 'KAKAO'
    source_id       VARCHAR2(200),                 -- 선택(원하면 place_id 문자열 복제)
                                                   -- ✅ 여기 UNIQUE 걸고 싶으면 유지 가능

    is_active       VARCHAR2(1) DEFAULT 'Y',       -- Y/N
    difficulty      VARCHAR2(10),
    distance_km     NUMBER,

    created_at      DATE,
    updated_at      DATE,

    CONSTRAINT fk_places_region
        FOREIGN KEY (region_id) REFERENCES REGION(region_id),

    CONSTRAINT fk_places_city
        FOREIGN KEY (city_id) REFERENCES CITY(city_id)
);

/* source_id를 UNIQUE로 유지하고 싶으면 아래 켜기 (선택)
ALTER TABLE PLACES ADD CONSTRAINT uk_places_source_id UNIQUE (source_id);
*/

/* =========================
   CREATE: PLACES_DESCRIPTION
========================= */
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE PLACES_DESCRIPTION CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

CREATE TABLE PLACES_DESCRIPTION (
    place_id        NUMBER PRIMARY KEY,     -- ✅ PK = place_id (공유 PK)
    description     CLOB,
    image_url       VARCHAR2(500),
    tags            VARCHAR2(500),
    mood_keywords   VARCHAR2(500),
    created_at      DATE,
    updated_at      DATE,
    CONSTRAINT fk_places_desc
        FOREIGN KEY (place_id) REFERENCES PLACES(place_id)
);

