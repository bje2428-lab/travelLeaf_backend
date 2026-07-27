CREATE TABLE REGION (
    region_id      NUMBER PRIMARY KEY,
    region_name    VARCHAR2(100) NOT NULL
);

CREATE SEQUENCE seq_region START WITH 1 INCREMENT BY 1;

CREATE TABLE CITY (
    city_id      NUMBER PRIMARY KEY,
    region_id    NUMBER NOT NULL,
    city_name    VARCHAR2(100) NOT NULL,

    CONSTRAINT fk_city_region
        FOREIGN KEY (region_id) REFERENCES REGION(region_id)
);

CREATE SEQUENCE seq_city START WITH 1 INCREMENT BY 1;

CREATE TABLE USER_TB (
    user_id      NUMBER PRIMARY KEY,
    email        VARCHAR2(200) UNIQUE,
    password     VARCHAR2(200),
    nickname     VARCHAR2(100),
    phone        VARCHAR2(50)
);

CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1;

CREATE TABLE PLACES (
    place_id        NUMBER PRIMARY KEY,
    name            VARCHAR2(200) NOT NULL,
    category        VARCHAR2(100),

    region_id       NUMBER,
    city_id         NUMBER,

    address         VARCHAR2(300),
    lat             NUMBER,
    lng             NUMBER,

    rating          NUMBER,
    source_type     VARCHAR2(50),
    source_id       VARCHAR2(100) UNIQUE,

    is_active       VARCHAR2(10),
    created_at      DATE,
    updated_at      DATE,

    CONSTRAINT fk_places_region
        FOREIGN KEY (region_id) REFERENCES REGION(region_id),

    CONSTRAINT fk_places_city
        FOREIGN KEY (city_id) REFERENCES CITY(city_id)
);

CREATE SEQUENCE seq_places START WITH 1 INCREMENT BY 1;

CREATE TABLE PLACES_PET (
    pet_id           NUMBER PRIMARY KEY,
    place_id         NUMBER NOT NULL,

    pet_allowed      VARCHAR2(10),
    pet_size_limit   VARCHAR2(100),
    pet_fee          VARCHAR2(100),
    pet_rules        VARCHAR2(1000),

    CONSTRAINT fk_places_pet
        FOREIGN KEY (place_id) REFERENCES PLACES(place_id)
);

CREATE SEQUENCE seq_places_pet START WITH 1 INCREMENT BY 1;

CREATE TABLE PLACES_DESCRIPTION (
    description_id   NUMBER PRIMARY KEY,
    place_id         NUMBER NOT NULL,

    description      CLOB,
    tags             VARCHAR2(500),
    image_url        VARCHAR2(500),

    CONSTRAINT fk_places_desc
        FOREIGN KEY (place_id) REFERENCES PLACES(place_id)
);

CREATE SEQUENCE seq_places_desc START WITH 1 INCREMENT BY 1;

CREATE TABLE SCHEDULE (
    schedule_id        NUMBER PRIMARY KEY,
    schedule_code      VARCHAR2(100) UNIQUE,
    user_id            NUMBER NOT NULL,

    region_id          NUMBER,
    city_id            NUMBER,

    start_region_id    NUMBER,
    start_city_id      NUMBER,
    end_region_id      NUMBER,
    end_city_id        NUMBER,

    schedule_title     VARCHAR2(200),
    start_date         DATE,
    end_date           DATE,
    people_count       NUMBER,
    budget             NUMBER,

    pet_travel         VARCHAR2(10),
    pet_type           VARCHAR2(50),
    pet_size           VARCHAR2(50),

    hashtags           VARCHAR2(500),
    ai_keywords        VARCHAR2(500),

    thumbnail_img      VARCHAR2(500),
    memo               VARCHAR2(1000),
    is_public          VARCHAR2(10),

    created_at         DATE,
    updated_at         DATE,

    CONSTRAINT fk_schedule_user
        FOREIGN KEY (user_id) REFERENCES USER_TB(user_id),

    CONSTRAINT fk_schedule_region
        FOREIGN KEY (region_id) REFERENCES REGION(region_id),

    CONSTRAINT fk_schedule_city
        FOREIGN KEY (city_id) REFERENCES CITY(city_id),

    CONSTRAINT fk_schedule_start_region
        FOREIGN KEY (start_region_id) REFERENCES REGION(region_id),

    CONSTRAINT fk_schedule_start_city
        FOREIGN KEY (start_city_id) REFERENCES CITY(city_id),

    CONSTRAINT fk_schedule_end_region
        FOREIGN KEY (end_region_id) REFERENCES REGION(region_id),

    CONSTRAINT fk_schedule_end_city
        FOREIGN KEY (end_city_id) REFERENCES CITY(city_id)
);

CREATE SEQUENCE seq_schedule START WITH 1 INCREMENT BY 1;

CREATE TABLE SCHEDULE_DETAIL (
    detail_id          NUMBER PRIMARY KEY,
    schedule_id        NUMBER NOT NULL,
    day_number         NUMBER NOT NULL,
    order_in_day       NUMBER NOT NULL,

    place_id           NUMBER,  -- NULL 허용 (직접입력 가능)

    custom_place_name  VARCHAR2(200),
    custom_address     VARCHAR2(300),

    start_time         VARCHAR2(50),
    end_time           VARCHAR2(50),

    transportation     VARCHAR2(100),
    cost               NUMBER,
    memo               VARCHAR2(1000),

    created_at         DATE,

    CONSTRAINT fk_detail_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES SCHEDULE(schedule_id),

    CONSTRAINT fk_detail_place
        FOREIGN KEY (place_id)
        REFERENCES PLACES(place_id)
);

CREATE SEQUENCE seq_schedule_detail START WITH 1 INCREMENT BY 1;
